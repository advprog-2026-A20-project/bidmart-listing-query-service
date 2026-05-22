package id.ac.ui.cs.advprog.listingquery.lifecycle;

import id.ac.ui.cs.advprog.listingquery.model.ListingStatus;

final class DraftListingState extends AbstractListingState {

    DraftListingState() {
        super(ListingStatus.DRAFT);
    }

    @Override
    public boolean canEdit() {
        return true;
    }

    @Override
    public boolean canReceiveBid() {
        return false;
    }
}
