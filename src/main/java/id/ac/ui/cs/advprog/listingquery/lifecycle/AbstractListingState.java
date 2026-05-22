package id.ac.ui.cs.advprog.listingquery.lifecycle;

import id.ac.ui.cs.advprog.listingquery.model.ListingStatus;

abstract class AbstractListingState implements ListingState {

    private final ListingStatus status;

    AbstractListingState(ListingStatus status) {
        this.status = status;
    }

    @Override
    public ListingStatus status() {
        return status;
    }
}
