package id.ac.ui.cs.advprog.listingquery.lifecycle;

import id.ac.ui.cs.advprog.listingquery.model.ListingStatus;

final class TerminalListingState extends AbstractListingState {

    TerminalListingState(ListingStatus status) {
        super(status);
    }

    @Override
    public boolean canEdit() {
        return false;
    }

    @Override
    public boolean canReceiveBid() {
        return false;
    }
}
