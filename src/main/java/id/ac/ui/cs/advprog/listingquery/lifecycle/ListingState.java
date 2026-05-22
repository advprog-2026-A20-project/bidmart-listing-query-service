package id.ac.ui.cs.advprog.listingquery.lifecycle;

import id.ac.ui.cs.advprog.listingquery.model.ListingStatus;

public interface ListingState {

    ListingStatus status();

    boolean canEdit();

    boolean canReceiveBid();
}
