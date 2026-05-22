package id.ac.ui.cs.advprog.listingquery.readmodel;

import id.ac.ui.cs.advprog.listingquery.lifecycle.ListingLifecyclePolicy;
import id.ac.ui.cs.advprog.listingquery.dto.ListingDetailResponse;
import id.ac.ui.cs.advprog.listingquery.dto.ListingResponse;
import id.ac.ui.cs.advprog.listingquery.model.Auction;
import id.ac.ui.cs.advprog.listingquery.model.Listing;
import id.ac.ui.cs.advprog.listingquery.repository.AuctionRepository;
import id.ac.ui.cs.advprog.listingquery.repository.BidRepository;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ListingReadModelAssembler {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ListingLifecyclePolicy lifecyclePolicy;
    private final ListingResponseMapper responseMapper;

    public ListingReadModelAssembler(
        AuctionRepository auctionRepository,
        BidRepository bidRepository,
        ListingLifecyclePolicy lifecyclePolicy,
        ListingResponseMapper responseMapper
    ) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.lifecyclePolicy = lifecyclePolicy;
        this.responseMapper = responseMapper;
    }

    public ListingResponse toSummaryResponse(Listing listing) {
        return responseMapper.toSummaryResponse(assemble(listing));
    }

    public ListingResponse toSummaryResponse(ListingReadModel readModel) {
        return responseMapper.toSummaryResponse(readModel);
    }

    public ListingDetailResponse toDetailResponse(Listing listing) {
        return responseMapper.toDetailResponse(assemble(listing));
    }

    public ListingDetailResponse toDetailResponse(Listing listing, Auction auction) {
        return responseMapper.toDetailResponse(assemble(listing, auction));
    }

    public ListingReadModel assemble(Listing listing) {
        Auction auction = auctionRepository.findByListingId(listing.getId()).orElse(null);
        return assemble(listing, auction);
    }

    public ListingReadModel assemble(Listing listing, Auction auction) {
        long totalBids = auction == null ? 0 : bidRepository.countByAuctionId(auction.getId());
        BigDecimal highestAmount = auction == null
            ? null
            : bidRepository.findHighestAmountByAuctionId(auction.getId()).orElse(null);
        return new ListingReadModel(
            listing,
            auction,
            lifecyclePolicy.effectiveStatus(listing, auction),
            resolveDisplayPrice(listing, auction, highestAmount),
            totalBids
        );
    }

    public List<ListingReadModel> assembleAll(List<Listing> listings) {
        if (listings.isEmpty()) {
            return List.of();
        }

        Map<UUID, Auction> auctionsByListingId = loadAuctionsByListingId(listings);
        Map<UUID, BidSummary> bidSummariesByAuctionId = loadBidSummariesByAuctionId(auctionsByListingId.values());

        return listings.stream()
            .map(listing -> {
                Auction auction = auctionsByListingId.get(listing.getId());
                BidSummary bidSummary = auction == null
                    ? BidSummary.empty()
                    : bidSummariesByAuctionId.getOrDefault(auction.getId(), BidSummary.empty());
                return new ListingReadModel(
                    listing,
                    auction,
                    lifecyclePolicy.effectiveStatus(listing, auction),
                    resolveDisplayPrice(listing, auction, bidSummary.highestAmount()),
                    bidSummary.totalBids()
                );
            })
            .toList();
    }

    private Map<UUID, Auction> loadAuctionsByListingId(List<Listing> listings) {
        List<UUID> listingIds = listings.stream()
            .map(Listing::getId)
            .toList();
        return auctionRepository.findByListingIdIn(listingIds).stream()
            .collect(Collectors.toMap(auction -> auction.getListing().getId(), Function.identity()));
    }

    private Map<UUID, BidSummary> loadBidSummariesByAuctionId(Collection<Auction> auctions) {
        if (auctions.isEmpty()) {
            return Collections.emptyMap();
        }

        List<UUID> auctionIds = auctions.stream()
            .map(Auction::getId)
            .toList();
        return bidRepository.summarizeByAuctionIds(auctionIds).stream()
            .collect(Collectors.toMap(
                BidRepository.AuctionBidSummary::getAuctionId,
                summary -> new BidSummary(summary.getTotalBids(), summary.getHighestAmount())
            ));
    }

    private BigDecimal resolveDisplayPrice(Listing listing, Auction auction, BigDecimal highestAmount) {
        if (auction == null) {
            return listing.getPrice();
        }
        return highestAmount == null ? auction.getStartingPrice() : highestAmount;
    }

    private record BidSummary(long totalBids, BigDecimal highestAmount) {

        static BidSummary empty() {
            return new BidSummary(0, null);
        }
    }
}
