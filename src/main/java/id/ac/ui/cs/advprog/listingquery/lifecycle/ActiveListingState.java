package id.ac.ui.cs.advprog.listingquery.lifecycle;

import id.ac.ui.cs.advprog.listingquery.model.ListingStatus;

final class ActiveListingState extends AbstractListingState {

    ActiveListingState() {
        super(ListingStatus.ACTIVE);
    }

    @Override
    public boolean canEdit() {
        return true;
    }

    @Override
    public boolean canReceiveBid() {
        return true;
    }
}
