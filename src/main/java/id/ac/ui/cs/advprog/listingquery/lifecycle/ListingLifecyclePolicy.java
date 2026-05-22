package id.ac.ui.cs.advprog.listingquery.lifecycle;

import id.ac.ui.cs.advprog.listingquery.model.Auction;
import id.ac.ui.cs.advprog.listingquery.model.AuctionStatus;
import id.ac.ui.cs.advprog.listingquery.model.Listing;
import id.ac.ui.cs.advprog.listingquery.model.ListingStatus;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ListingLifecyclePolicy {

    private final Map<ListingStatus, ListingState> states;

    public ListingLifecyclePolicy() {
        this(createDefaultStates());
    }

    ListingLifecyclePolicy(Map<ListingStatus, ListingState> states) {
        this.states = states;
    }

    public static ListingLifecyclePolicy createDefault() {
        return new ListingLifecyclePolicy(createDefaultStates());
    }

    public ListingStatus effectiveStatus(Listing listing, Auction auction) {
        if (listing.getStatus() == ListingStatus.CANCELLED) {
            return ListingStatus.CANCELLED;
        }
        if (auction == null || auction.getStatus() == null) {
            return listing.getStatus();
        }
        return toListingStatus(auction.getStatus());
    }

    public boolean canReceiveBid(ListingStatus status) {
        return stateFor(status).canReceiveBid();
    }

    public boolean canEdit(ListingStatus status) {
        return stateFor(status).canEdit();
    }

    public boolean canCancelAuction(AuctionStatus status) {
        return status == AuctionStatus.DRAFT || status == AuctionStatus.ACTIVE;
    }

    public ListingStatus toListingStatus(AuctionStatus auctionStatus) {
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

    private ListingState stateFor(ListingStatus status) {
        ListingState state = states.get(status);
        if (state == null) {
            throw new IllegalArgumentException("Unsupported listing status: " + status);
        }
        return state;
    }

    private static Map<ListingStatus, ListingState> createDefaultStates() {
        Map<ListingStatus, ListingState> defaultStates = new EnumMap<>(ListingStatus.class);
        defaultStates.put(ListingStatus.DRAFT, new DraftListingState());
        defaultStates.put(ListingStatus.ACTIVE, new ActiveListingState());
        defaultStates.put(ListingStatus.EXTENDED, new ExtendedListingState());
        defaultStates.put(ListingStatus.CLOSED, new TerminalListingState(ListingStatus.CLOSED));
        defaultStates.put(ListingStatus.WON, new TerminalListingState(ListingStatus.WON));
        defaultStates.put(ListingStatus.UNSOLD, new TerminalListingState(ListingStatus.UNSOLD));
        defaultStates.put(ListingStatus.CANCELLED, new TerminalListingState(ListingStatus.CANCELLED));
        return Map.copyOf(defaultStates);
    }
}
