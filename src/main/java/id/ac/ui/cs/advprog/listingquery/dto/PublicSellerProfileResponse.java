package id.ac.ui.cs.advprog.listingquery.dto;

import id.ac.ui.cs.advprog.listingquery.model.Role;
import java.util.UUID;

public record PublicSellerProfileResponse(
    UUID id,
    String email,
    Role role,
    long activeListingCount,
    long liveAuctionCount,
    long completedAuctionCount
) {
}
