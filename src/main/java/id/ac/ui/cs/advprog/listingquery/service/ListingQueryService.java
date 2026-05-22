package id.ac.ui.cs.advprog.listingquery.service;

import id.ac.ui.cs.advprog.listingquery.dto.ListingBidValidationResponse;
import id.ac.ui.cs.advprog.listingquery.dto.ListingCategoryNodeResponse;
import id.ac.ui.cs.advprog.listingquery.dto.ListingCreateRequest;
import id.ac.ui.cs.advprog.listingquery.dto.ListingDetailResponse;
import id.ac.ui.cs.advprog.listingquery.dto.ListingResponse;
import id.ac.ui.cs.advprog.listingquery.dto.ListingUpdateRequest;
import id.ac.ui.cs.advprog.listingquery.dto.PublicSellerProfileResponse;
import id.ac.ui.cs.advprog.listingquery.factory.ListingFactory;
import id.ac.ui.cs.advprog.listingquery.filter.ListingFilterCriteria;
import id.ac.ui.cs.advprog.listingquery.filter.ListingSpecificationBuilder;
import id.ac.ui.cs.advprog.listingquery.lifecycle.ListingLifecyclePolicy;
import id.ac.ui.cs.advprog.listingquery.model.Auction;
import id.ac.ui.cs.advprog.listingquery.model.AuctionStatus;
import id.ac.ui.cs.advprog.listingquery.model.Listing;
import id.ac.ui.cs.advprog.listingquery.model.ListingCategory;
import id.ac.ui.cs.advprog.listingquery.model.ListingStatus;
import id.ac.ui.cs.advprog.listingquery.model.Role;
import id.ac.ui.cs.advprog.listingquery.model.User;
import id.ac.ui.cs.advprog.listingquery.repository.AuctionRepository;
import id.ac.ui.cs.advprog.listingquery.repository.BidRepository;
import id.ac.ui.cs.advprog.listingquery.repository.ListingRepository;
import id.ac.ui.cs.advprog.listingquery.repository.UserRepository;
import id.ac.ui.cs.advprog.listingquery.readmodel.ListingReadModel;
import id.ac.ui.cs.advprog.listingquery.readmodel.ListingReadModelAssembler;
import id.ac.ui.cs.advprog.listingquery.validation.ListingRequestValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ListingQueryService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;
    private static final List<AuctionStatus> LIVE_AUCTION_STATUSES = List.of(
        AuctionStatus.DRAFT,
        AuctionStatus.ACTIVE,
        AuctionStatus.EXTENDED
    );
    private static final List<AuctionStatus> COMPLETED_AUCTION_STATUSES = List.of(
        AuctionStatus.CLOSED,
        AuctionStatus.WON,
        AuctionStatus.UNSOLD,
        AuctionStatus.CANCELLED
    );

    private final ListingRepository listingRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final ListingRequestValidator requestValidator;
    private final ListingFactory listingFactory;
    private final ListingLifecyclePolicy lifecyclePolicy;
    private final ListingSpecificationBuilder specificationBuilder;
    private final ListingReadModelAssembler readModelAssembler;

    public ListingQueryService(
        ListingRepository listingRepository,
        AuctionRepository auctionRepository,
        BidRepository bidRepository,
        UserRepository userRepository,
        ListingRequestValidator requestValidator,
        ListingFactory listingFactory,
        ListingLifecyclePolicy lifecyclePolicy,
        ListingSpecificationBuilder specificationBuilder,
        ListingReadModelAssembler readModelAssembler
    ) {
        this.listingRepository = listingRepository;
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.userRepository = userRepository;
        this.requestValidator = requestValidator;
        this.listingFactory = listingFactory;
        this.lifecyclePolicy = lifecyclePolicy;
        this.specificationBuilder = specificationBuilder;
        this.readModelAssembler = readModelAssembler;
    }

    @Transactional
    public ListingResponse createListing(ListingCreateRequest request, UUID sellerId) {
        requestValidator.validateCreateRequest(request);
        User seller = loadAuthorizedSeller(sellerId);
        Listing listing = listingFactory.createActiveListing(request, seller, Instant.now());
        return readModelAssembler.toSummaryResponse(listingRepository.save(listing));
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getAllListings(
        Pageable pageable,
        ListingCategory category,
        String keyword,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        ListingStatus status,
        Instant endingAfter,
        Instant endingBefore
    ) {
        requestValidator.validatePriceRange(minPrice, maxPrice);

        int requestedPageSize = pageable.isPaged() ? pageable.getPageSize() : DEFAULT_PAGE_SIZE;
        int safePageNumber = pageable.isPaged() ? Math.max(pageable.getPageNumber(), 0) : 0;
        Sort safeSort = pageable.getSort().isSorted()
            ? pageable.getSort()
            : Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable safePageable = PageRequest.of(
            safePageNumber,
            Math.max(1, Math.min(requestedPageSize, MAX_PAGE_SIZE)),
            safeSort
        );

        Specification<Listing> specification = specificationBuilder.build(
            new ListingFilterCriteria(category, keyword, minPrice, maxPrice)
        );

        List<Listing> matchingListings = listingRepository.findAll(specification, safeSort);
        List<ListingReadModel> filteredReadModels = readModelAssembler.assembleAll(matchingListings).stream()
            .filter(readModel -> matchesStatusFilter(readModel, status))
            .filter(readModel -> matchesAuctionWindow(readModel.auction(), endingAfter, endingBefore))
            .toList();

        int fromIndex = Math.min((int) safePageable.getOffset(), filteredReadModels.size());
        int toIndex = Math.min(fromIndex + safePageable.getPageSize(), filteredReadModels.size());
        return filteredReadModels.subList(fromIndex, toIndex).stream()
            .map(readModelAssembler::toSummaryResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ListingDetailResponse getListingDetail(UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        Auction auction = findAuctionByListingId(listingId).orElse(null);
        return readModelAssembler.toDetailResponse(listing, auction);
    }

    @Transactional(readOnly = true)
    public List<ListingCategory> getCategories() {
        return Arrays.stream(ListingCategory.values()).toList();
    }

    @Transactional(readOnly = true)
    public List<ListingCategoryNodeResponse> getCategoryTree() {
        return Arrays.stream(ListingCategory.values())
            .filter(ListingCategory::isRoot)
            .map(this::toCategoryNode)
            .toList();
    }

    @Transactional
    public ListingDetailResponse updateListing(UUID listingId, ListingUpdateRequest request, UUID sellerId) {
        requestValidator.validateUpdateRequest(request);
        Listing listing = getOwnedEditableListing(listingId, sellerId);
        listingFactory.applyEditableUpdate(listing, request, Instant.now());
        return readModelAssembler.toDetailResponse(listingRepository.save(listing));
    }

    @Transactional
    public ListingDetailResponse cancelListing(UUID listingId, UUID sellerId) {
        Listing listing = getOwnedEditableListing(listingId, sellerId);
        findAuctionByListingId(listingId).ifPresent(auction -> {
            if (!lifecyclePolicy.canCancelAuction(auction.getStatus())) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Auction cannot be cancelled in status " + auction.getStatus()
                );
            }
            auction.setStatus(AuctionStatus.CANCELLED);
            auction.setClosedAt(Instant.now());
        });
        listing.setStatus(ListingStatus.CANCELLED);
        listing.setCancelledAt(Instant.now());
        listing.setUpdatedAt(Instant.now());
        return readModelAssembler.toDetailResponse(listingRepository.save(listing));
    }

    @Transactional(readOnly = true)
    public ListingBidValidationResponse validateListingForBid(UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        Auction auction = findAuctionByListingId(listingId).orElse(null);

        ListingStatus listingStatus = lifecyclePolicy.effectiveStatus(listing, auction);
        boolean active = lifecyclePolicy.canReceiveBid(listingStatus);
        if (!active) {
            return new ListingBidValidationResponse(
                listing.getId(),
                false,
                false,
                "Listing is no longer active",
                listingStatus,
                auction == null ? null : auction.getStatus(),
                auction == null ? null : auction.getEndsAt()
            );
        }
        if (auction == null) {
            return new ListingBidValidationResponse(
                listing.getId(),
                true,
                false,
                "Listing is not attached to an auction",
                listingStatus,
                null,
                null
            );
        }

        boolean biddable = auction.getStatus() == AuctionStatus.ACTIVE || auction.getStatus() == AuctionStatus.EXTENDED;
        return new ListingBidValidationResponse(
            listing.getId(),
            true,
            biddable,
            biddable ? "Listing is valid for bidding" : "Auction is not accepting bids",
            listingStatus,
            auction.getStatus(),
            auction.getEndsAt()
        );
    }

    @Transactional(readOnly = true)
    public PublicSellerProfileResponse getPublicSellerProfile(UUID userId) {
        User seller = userRepository.findById(userId)
            .filter(user -> user.getRole() == Role.SELLER)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found"));

        long activeListingCount = listingRepository.countBySellerIdAndStatus(seller.getId(), ListingStatus.ACTIVE);
        long liveAuctionCount = auctionRepository.countByListingSellerIdAndStatusIn(
            seller.getId(),
            LIVE_AUCTION_STATUSES
        );
        long completedAuctionCount = auctionRepository.countByListingSellerIdAndStatusIn(
            seller.getId(),
            COMPLETED_AUCTION_STATUSES
        );

        return new PublicSellerProfileResponse(
            seller.getId(),
            seller.getEmail(),
            seller.getRole(),
            activeListingCount,
            liveAuctionCount,
            completedAuctionCount
        );
    }

    private boolean matchesAuctionWindow(Auction auction, Instant endingAfter, Instant endingBefore) {
        if (endingAfter == null && endingBefore == null) {
            return true;
        }

        if (auction == null || auction.getEndsAt() == null) {
            return false;
        }

        boolean matchesAfter = endingAfter == null || !auction.getEndsAt().isBefore(endingAfter);
        boolean matchesBefore = endingBefore == null || !auction.getEndsAt().isAfter(endingBefore);
        return matchesAfter && matchesBefore;
    }

    private Optional<Auction> findAuctionByListingId(UUID listingId) {
        return auctionRepository.findByListingId(listingId);
    }

    private Listing getOwnedEditableListing(UUID listingId, UUID sellerId) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        Auction auction = findAuctionByListingId(listingId).orElse(null);

        if (!listing.getSeller().getId().equals(sellerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this listing");
        }
        if (auction != null && auction.getStatus() != AuctionStatus.DRAFT && auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Listing cannot be modified in auction status " + auction.getStatus()
            );
        }
        if (!lifecyclePolicy.canEdit(lifecyclePolicy.effectiveStatus(listing, auction))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Listing is not editable");
        }
        if (bidRepository.existsByListingId(listingId)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Listing cannot be modified because it already has bids"
            );
        }

        return listing;
    }

    private boolean matchesStatusFilter(ListingReadModel readModel, ListingStatus requestedStatus) {
        if (requestedStatus == null) {
            return true;
        }
        return readModel.status() == requestedStatus;
    }

    private ListingCategoryNodeResponse toCategoryNode(ListingCategory category) {
        return new ListingCategoryNodeResponse(
            category,
            category.label(),
            category.pathLabel(),
            category.children().stream().map(this::toCategoryNode).toList()
        );
    }

    private User loadAuthorizedSeller(UUID sellerId) {
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (seller.getRole() != Role.SELLER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only SELLER can create listings");
        }
        return seller;
    }

}
