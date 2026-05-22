package id.ac.ui.cs.advprog.listingquery.lifecycle;

import id.ac.ui.cs.advprog.listingquery.model.ListingStatus;

final class ExtendedListingState extends AbstractListingState {

    ExtendedListingState() {
        super(ListingStatus.EXTENDED);
    }

    @Override
    public boolean canEdit() {
        return false;
    }

    @Override
    public boolean canReceiveBid() {
        return true;
    }
}
