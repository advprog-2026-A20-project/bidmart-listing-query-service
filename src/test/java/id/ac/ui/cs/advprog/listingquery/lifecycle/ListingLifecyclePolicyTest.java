package id.ac.ui.cs.advprog.listingquery.lifecycle;

import id.ac.ui.cs.advprog.listingquery.model.Auction;
import id.ac.ui.cs.advprog.listingquery.model.AuctionStatus;
import id.ac.ui.cs.advprog.listingquery.model.Listing;
import id.ac.ui.cs.advprog.listingquery.model.ListingStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ListingLifecyclePolicyTest {

    private final ListingLifecyclePolicy lifecyclePolicy = ListingLifecyclePolicy.createDefault();

    @Test
    void effectiveStatusShouldFollowAuctionStatusUnlessListingIsCancelled() {
        Listing listing = Listing.builder()
            .status(ListingStatus.EXTENDED)
            .build();
        Auction wonAuction = auctionWithStatus(AuctionStatus.WON);

        assertThat(lifecyclePolicy.effectiveStatus(listing, wonAuction)).isEqualTo(ListingStatus.WON);

        listing.setStatus(ListingStatus.CANCELLED);

        assertThat(lifecyclePolicy.effectiveStatus(listing, wonAuction)).isEqualTo(ListingStatus.CANCELLED);
    }

    @Test
    void onlyActiveAndExtendedListingsCanReceiveBids() {
        assertThat(lifecyclePolicy.canReceiveBid(ListingStatus.ACTIVE)).isTrue();
        assertThat(lifecyclePolicy.canReceiveBid(ListingStatus.EXTENDED)).isTrue();
        assertThat(lifecyclePolicy.canReceiveBid(ListingStatus.DRAFT)).isFalse();
        assertThat(lifecyclePolicy.canReceiveBid(ListingStatus.WON)).isFalse();
        assertThat(lifecyclePolicy.canReceiveBid(ListingStatus.UNSOLD)).isFalse();
        assertThat(lifecyclePolicy.canReceiveBid(ListingStatus.CANCELLED)).isFalse();
    }

    @Test
    void onlyDraftAndActiveListingsCanBeEditedBeforeBid() {
        assertThat(lifecyclePolicy.canEdit(ListingStatus.DRAFT)).isTrue();
        assertThat(lifecyclePolicy.canEdit(ListingStatus.ACTIVE)).isTrue();
        assertThat(lifecyclePolicy.canEdit(ListingStatus.EXTENDED)).isFalse();
        assertThat(lifecyclePolicy.canEdit(ListingStatus.CLOSED)).isFalse();
        assertThat(lifecyclePolicy.canEdit(ListingStatus.WON)).isFalse();
    }

    @Test
    void onlyDraftAndActiveAuctionsCanBeCancelledBySeller() {
        assertThat(lifecyclePolicy.canCancelAuction(AuctionStatus.DRAFT)).isTrue();
        assertThat(lifecyclePolicy.canCancelAuction(AuctionStatus.ACTIVE)).isTrue();
        assertThat(lifecyclePolicy.canCancelAuction(AuctionStatus.EXTENDED)).isFalse();
        assertThat(lifecyclePolicy.canCancelAuction(AuctionStatus.CANCELLED)).isFalse();
    }

    private Auction auctionWithStatus(AuctionStatus status) {
        Auction auction = new Auction();
        auction.setStatus(status);
        return auction;
    }
}
