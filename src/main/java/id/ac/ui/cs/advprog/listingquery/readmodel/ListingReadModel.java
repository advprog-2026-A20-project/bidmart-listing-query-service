package id.ac.ui.cs.advprog.listingquery.readmodel;

import id.ac.ui.cs.advprog.listingquery.model.Auction;
import id.ac.ui.cs.advprog.listingquery.model.Listing;
import id.ac.ui.cs.advprog.listingquery.model.ListingStatus;
import java.math.BigDecimal;

public record ListingReadModel(
    Listing listing,
    Auction auction,
    ListingStatus status,
    BigDecimal displayPrice,
    long totalBids
) {

    public boolean hasBids() {
        return totalBids > 0;
    }
}
