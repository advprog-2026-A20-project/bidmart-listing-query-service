package id.ac.ui.cs.advprog.listingquery.factory;

import id.ac.ui.cs.advprog.listingquery.dto.ListingCreateRequest;
import id.ac.ui.cs.advprog.listingquery.dto.ListingUpdateRequest;
import id.ac.ui.cs.advprog.listingquery.model.Listing;
import id.ac.ui.cs.advprog.listingquery.model.ListingCategory;
import id.ac.ui.cs.advprog.listingquery.model.ListingStatus;
import id.ac.ui.cs.advprog.listingquery.model.Role;
import id.ac.ui.cs.advprog.listingquery.model.User;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ListingFactoryTest {

    private final ListingFactory listingFactory = new ListingFactory();

    @Test
    void createActiveListingShouldNormalizeInputAndDefaultCategory() {
        User seller = seller();
        Instant createdAt = Instant.parse("2026-05-22T10:00:00Z");
        ListingCreateRequest request = new ListingCreateRequest(
            "  Phone  ",
            "  Nice phone  ",
            "  https://img.example/phone.jpg  ",
            BigDecimal.valueOf(1200),
            null
        );

        Listing listing = listingFactory.createActiveListing(request, seller, createdAt);

        assertThat(listing.getTitle()).isEqualTo("Phone");
        assertThat(listing.getDescription()).isEqualTo("Nice phone");
        assertThat(listing.getImageUrl()).isEqualTo("https://img.example/phone.jpg");
        assertThat(listing.getCategory()).isEqualTo(ListingCategory.OTHER);
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.ACTIVE);
        assertThat(listing.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void applyEditableUpdateShouldOnlyChangeEditableListingFields() {
        Listing listing = Listing.builder()
            .title("Original")
            .description("Old description")
            .imageUrl("https://img.example/old.jpg")
            .price(BigDecimal.valueOf(1200))
            .category(ListingCategory.ELECTRONICS)
            .status(ListingStatus.ACTIVE)
            .build();
        Instant updatedAt = Instant.parse("2026-05-22T11:00:00Z");
        ListingUpdateRequest request = new ListingUpdateRequest(
            "  New description  ",
            " ",
            null
        );

        listingFactory.applyEditableUpdate(listing, request, updatedAt);

        assertThat(listing.getTitle()).isEqualTo("Original");
        assertThat(listing.getDescription()).isEqualTo("New description");
        assertThat(listing.getImageUrl()).isNull();
        assertThat(listing.getCategory()).isEqualTo(ListingCategory.OTHER);
        assertThat(listing.getUpdatedAt()).isEqualTo(updatedAt);
    }

    private User seller() {
        User seller = new User();
        ReflectionTestUtils.setField(seller, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(seller, "email", "seller@example.com");
        ReflectionTestUtils.setField(seller, "role", Role.SELLER);
        return seller;
    }
}
