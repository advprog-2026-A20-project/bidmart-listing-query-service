package id.ac.ui.cs.advprog.listingquery.service;

import id.ac.ui.cs.advprog.listingquery.dto.ListingBidValidationResponse;
import id.ac.ui.cs.advprog.listingquery.dto.ListingCategoryNodeResponse;
import id.ac.ui.cs.advprog.listingquery.dto.ListingCreateRequest;
import id.ac.ui.cs.advprog.listingquery.dto.ListingDetailResponse;
import id.ac.ui.cs.advprog.listingquery.dto.ListingResponse;
import id.ac.ui.cs.advprog.listingquery.dto.ListingUpdateRequest;
import id.ac.ui.cs.advprog.listingquery.dto.PublicSellerProfileResponse;
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
    private static final List<ListingStatus> PUBLIC_LISTING_STATUSES = List.of(
        ListingStatus.ACTIVE,
        ListingStatus.EXTENDED
    );
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

    public ListingQueryService(
        ListingRepository listingRepository,
        AuctionRepository auctionRepository,
        BidRepository bidRepository,
        UserRepository userRepository
    ) {
        this.listingRepository = listingRepository;
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ListingResponse createListing(ListingCreateRequest request, UUID sellerId) {
        validateCreateRequest(request);
        User seller = loadAuthorizedSeller(sellerId);
        Listing listing = Listing.builder()
            .title(request.title().trim())
            .description(request.description().trim())
            .imageUrl(normalizeImageUrl(request.imageUrl()))
            .price(request.price())
            .category(resolveCategory(request.category()))
            .seller(seller)
            .status(ListingStatus.ACTIVE)
            .createdAt(Instant.now())
            .build();
        return toSummaryResponse(listingRepository.save(listing));
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
        validatePriceRange(minPrice, maxPrice);

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

        Specification<Listing> specification = distinctResults()
            .and(hasCategoryOrDescendant(category))
            .and(matchesKeyword(keyword))
            .and(hasMinPrice(minPrice))
            .and(hasMaxPrice(maxPrice));

        List<Listing> matchingListings = listingRepository.findAll(specification, safeSort);
        List<Listing> filteredListings = matchingListings.stream()
            .filter(listing -> matchesStatusFilter(listing, status))
            .filter(listing -> matchesAuctionWindow(listing.getId(), endingAfter, endingBefore))
            .toList();

        int fromIndex = Math.min((int) safePageable.getOffset(), filteredListings.size());
        int toIndex = Math.min(fromIndex + safePageable.getPageSize(), filteredListings.size());
        return filteredListings.subList(fromIndex, toIndex).stream()
            .map(this::toSummaryResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ListingDetailResponse getListingDetail(UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        Auction auction = findAuctionByListingId(listingId).orElse(null);
        if (effectiveListingStatus(listing, auction) == ListingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found");
        }
        return toDetailResponse(listing, auction);
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
        validateUpdateRequest(request);
        Listing listing = getOwnedEditableListing(listingId, sellerId);
        listing.setDescription(request.description().trim());
        listing.setImageUrl(normalizeImageUrl(request.imageUrl()));
        listing.setCategory(resolveCategory(request.category()));
        listing.setUpdatedAt(Instant.now());
        return toDetailResponse(listingRepository.save(listing));
    }

    @Transactional
    public ListingDetailResponse cancelListing(UUID listingId, UUID sellerId) {
        Listing listing = getOwnedEditableListing(listingId, sellerId);
        findAuctionByListingId(listingId).ifPresent(auction -> {
            if (auction.getStatus() != AuctionStatus.DRAFT && auction.getStatus() != AuctionStatus.ACTIVE) {
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
        return toDetailResponse(listingRepository.save(listing));
    }

    @Transactional(readOnly = true)
    public ListingBidValidationResponse validateListingForBid(UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        Auction auction = findAuctionByListingId(listingId).orElse(null);

        ListingStatus listingStatus = effectiveListingStatus(listing, auction);
        boolean active = isListingOpenForBid(listingStatus);
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

    private ListingResponse toSummaryResponse(Listing listing) {
        Auction auction = findAuctionByListingId(listing.getId()).orElse(null);
        long totalBids = auction == null ? 0 : bidRepository.countByAuctionId(auction.getId());
        ListingStatus listingStatus = effectiveListingStatus(listing, auction);

        return new ListingResponse(
            listing.getId(),
            listing.getTitle(),
            listing.getDescription(),
            listing.getImageUrl(),
            listing.getPrice(),
            listing.getCategory(),
            listing.getCategory().pathLabel(),
            listing.getSeller().getId(),
            listing.getSeller().getEmail(),
            listingStatus,
            auction == null ? null : auction.getId(),
            auction == null ? null : auction.getStatus(),
            auction == null ? null : auction.getEndsAt(),
            totalBids,
            totalBids > 0,
            listing.getCreatedAt(),
            listing.getUpdatedAt(),
            listing.getCancelledAt()
        );
    }

    private ListingDetailResponse toDetailResponse(Listing listing) {
        Auction auction = findAuctionByListingId(listing.getId()).orElse(null);
        return toDetailResponse(listing, auction);
    }

    private ListingDetailResponse toDetailResponse(Listing listing, Auction auction) {
        long totalBids = auction == null ? 0 : bidRepository.countByAuctionId(auction.getId());
        ListingStatus listingStatus = effectiveListingStatus(listing, auction);

        return new ListingDetailResponse(
            listing.getId(),
            listing.getTitle(),
            listing.getDescription(),
            listing.getImageUrl(),
            listing.getPrice(),
            auction == null ? null : auction.getStartingPrice(),
            auction == null ? null : auction.getReservePrice(),
            auction == null ? null : auction.getMinimumBidIncrement(),
            auction == null ? null : auction.getDurationMinutes(),
            listing.getCategory(),
            listing.getCategory().pathLabel(),
            listing.getSeller().getId(),
            listing.getSeller().getEmail(),
            listingStatus,
            auction == null ? null : auction.getId(),
            auction == null ? null : auction.getStatus(),
            auction == null ? null : auction.getStartsAt(),
            auction == null ? null : auction.getEndsAt(),
            auction == null ? null : auction.getClosedAt(),
            totalBids,
            totalBids > 0,
            listing.getCreatedAt(),
            listing.getUpdatedAt(),
            listing.getCancelledAt()
        );
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "minPrice cannot be greater than maxPrice"
            );
        }
    }

    private Specification<Listing> distinctResults() {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            return criteriaBuilder.conjunction();
        };
    }

    private Specification<Listing> hasCategoryOrDescendant(ListingCategory category) {
        if (category == null) {
            return null;
        }

        List<ListingCategory> matchingCategories = Arrays.stream(ListingCategory.values())
            .filter(candidate -> candidate.isSameOrDescendantOf(category))
            .toList();

        return (root, query, criteriaBuilder) -> root.get("category").in(matchingCategories);
    }

    private Specification<Listing> matchesKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String normalizedKeyword = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), normalizedKeyword),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), normalizedKeyword)
        );
    }

    private Specification<Listing> hasMinPrice(BigDecimal minPrice) {
        if (minPrice == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    private Specification<Listing> hasMaxPrice(BigDecimal maxPrice) {
        if (maxPrice == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    private boolean matchesAuctionWindow(UUID listingId, Instant endingAfter, Instant endingBefore) {
        if (endingAfter == null && endingBefore == null) {
            return true;
        }

        Auction auction = findAuctionByListingId(listingId).orElse(null);
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
        if (!isListingEditableStatus(effectiveListingStatus(listing, auction))) {
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

    private boolean isPublicListing(Listing listing) {
        Auction auction = findAuctionByListingId(listing.getId()).orElse(null);
        return PUBLIC_LISTING_STATUSES.contains(effectiveListingStatus(listing, auction));
    }

    private boolean matchesStatusFilter(Listing listing, ListingStatus requestedStatus) {
        Auction auction = findAuctionByListingId(listing.getId()).orElse(null);
        ListingStatus effectiveStatus = effectiveListingStatus(listing, auction);
        if (requestedStatus == null) {
            return true;
        }
        return effectiveStatus == requestedStatus;
    }

    private boolean isListingOpenForBid(ListingStatus status) {
        return status == ListingStatus.ACTIVE || status == ListingStatus.EXTENDED;
    }

    private boolean isListingEditableStatus(ListingStatus status) {
        return status == ListingStatus.DRAFT || status == ListingStatus.ACTIVE;
    }

    private ListingStatus effectiveListingStatus(Listing listing, Auction auction) {
        if (listing.getStatus() == ListingStatus.CANCELLED) {
            return ListingStatus.CANCELLED;
        }
        if (auction == null || auction.getStatus() == null) {
            return listing.getStatus();
        }
        return toListingStatus(auction.getStatus());
    }

    private ListingStatus toListingStatus(AuctionStatus auctionStatus) {
        return switch (auctionStatus) {
            case DRAFT -> ListingStatus.DRAFT;
            case ACTIVE -> ListingStatus.ACTIVE;
            case EXTENDED -> ListingStatus.EXTENDED;
            case CLOSED -> ListingStatus.CLOSED;
            case WON -> ListingStatus.WON;
            case UNSOLD -> ListingStatus.UNSOLD;
            case CANCELLED -> ListingStatus.CANCELLED;
        };
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

    private ListingCategory resolveCategory(ListingCategory category) {
        return category != null ? category : ListingCategory.OTHER;
    }

    private void validateCreateRequest(ListingCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Listing request is required");
        }
        requireNonBlank(request.title(), "Title is required");
        requireNonBlank(request.description(), "Description is required");
        validatePositivePrice(request.price());
    }

    private void validateUpdateRequest(ListingUpdateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Listing update request is required");
        }
        requireNonBlank(request.description(), "Description is required");
    }

    private void requireNonBlank(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }

    private void validatePositivePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be positive");
        }
    }

    private String normalizeImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        return imageUrl.trim();
    }
}
