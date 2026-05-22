package id.ac.ui.cs.advprog.listingquery.factory;

import id.ac.ui.cs.advprog.listingquery.dto.ListingCreateRequest;
import id.ac.ui.cs.advprog.listingquery.dto.ListingUpdateRequest;
import id.ac.ui.cs.advprog.listingquery.model.Listing;
import id.ac.ui.cs.advprog.listingquery.model.ListingCategory;
import id.ac.ui.cs.advprog.listingquery.model.ListingStatus;
import id.ac.ui.cs.advprog.listingquery.model.User;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class ListingFactory {

    public Listing createActiveListing(ListingCreateRequest request, User seller, Instant createdAt) {
        return Listing.builder()
            .title(request.title().trim())
            .description(request.description().trim())
            .imageUrl(normalizeImageUrl(request.imageUrl()))
            .price(request.price())
            .category(resolveCategory(request.category()))
            .seller(seller)
            .status(ListingStatus.ACTIVE)
            .createdAt(createdAt)
            .build();
    }

    public void applyEditableUpdate(Listing listing, ListingUpdateRequest request, Instant updatedAt) {
        listing.setDescription(request.description().trim());
        listing.setImageUrl(normalizeImageUrl(request.imageUrl()));
        listing.setCategory(resolveCategory(request.category()));
        listing.setUpdatedAt(updatedAt);
    }

    private ListingCategory resolveCategory(ListingCategory category) {
        return category != null ? category : ListingCategory.OTHER;
    }

    private String normalizeImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        return imageUrl.trim();
    }
}
