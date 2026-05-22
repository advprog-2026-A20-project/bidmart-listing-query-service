package id.ac.ui.cs.advprog.listingquery.readmodel;

import id.ac.ui.cs.advprog.listingquery.lifecycle.ListingLifecyclePolicy;
import id.ac.ui.cs.advprog.listingquery.dto.ListingDetailResponse;
import id.ac.ui.cs.advprog.listingquery.dto.ListingResponse;
import id.ac.ui.cs.advprog.listingquery.model.Auction;
import id.ac.ui.cs.advprog.listingquery.model.Listing;
import id.ac.ui.cs.advprog.listingquery.repository.AuctionRepository;
import id.ac.ui.cs.advprog.listingquery.repository.BidRepository;
import java.math.BigDecimal;
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
        return new ListingReadModel(
            listing,
            auction,
            lifecyclePolicy.effectiveStatus(listing, auction),
            resolveDisplayPrice(listing, auction),
            totalBids
        );
    }

    private BigDecimal resolveDisplayPrice(Listing listing, Auction auction) {
        if (auction == null) {
            return listing.getPrice();
        }
        return bidRepository.findHighestAmountByAuctionId(auction.getId())
            .orElse(auction.getStartingPrice());
    }
}
