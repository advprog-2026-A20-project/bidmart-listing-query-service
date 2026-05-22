package id.ac.ui.cs.advprog.listingquery.readmodel;

import id.ac.ui.cs.advprog.listingquery.dto.ListingDetailResponse;
import id.ac.ui.cs.advprog.listingquery.dto.ListingResponse;
import id.ac.ui.cs.advprog.listingquery.model.Auction;
import id.ac.ui.cs.advprog.listingquery.model.Listing;
import org.springframework.stereotype.Component;

@Component
public class ListingResponseMapper {

    public ListingResponse toSummaryResponse(ListingReadModel readModel) {
        Listing listing = readModel.listing();
        Auction auction = readModel.auction();

        return new ListingResponse(
            listing.getId(),
            listing.getTitle(),
            listing.getDescription(),
            listing.getImageUrl(),
            readModel.displayPrice(),
            listing.getCategory(),
            listing.getCategory().pathLabel(),
            listing.getSeller().getId(),
            listing.getSeller().getEmail(),
            readModel.status(),
            auction == null ? null : auction.getId(),
            auction == null ? null : auction.getStatus(),
            auction == null ? null : auction.getEndsAt(),
            readModel.totalBids(),
            readModel.hasBids(),
            listing.getCreatedAt(),
            listing.getUpdatedAt(),
            listing.getCancelledAt()
        );
    }

    public ListingDetailResponse toDetailResponse(ListingReadModel readModel) {
        Listing listing = readModel.listing();
        Auction auction = readModel.auction();

        return new ListingDetailResponse(
            listing.getId(),
            listing.getTitle(),
            listing.getDescription(),
            listing.getImageUrl(),
            readModel.displayPrice(),
            auction == null ? null : auction.getStartingPrice(),
            auction == null ? null : auction.getReservePrice(),
            auction == null ? null : auction.getMinimumBidIncrement(),
            auction == null ? null : auction.getDurationMinutes(),
            listing.getCategory(),
            listing.getCategory().pathLabel(),
            listing.getSeller().getId(),
            listing.getSeller().getEmail(),
            readModel.status(),
            auction == null ? null : auction.getId(),
            auction == null ? null : auction.getStatus(),
            auction == null ? null : auction.getStartsAt(),
            auction == null ? null : auction.getEndsAt(),
            auction == null ? null : auction.getClosedAt(),
            readModel.totalBids(),
            readModel.hasBids(),
            listing.getCreatedAt(),
            listing.getUpdatedAt(),
            listing.getCancelledAt()
        );
    }
}
