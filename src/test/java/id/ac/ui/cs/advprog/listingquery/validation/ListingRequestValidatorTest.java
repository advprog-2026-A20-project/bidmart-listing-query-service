package id.ac.ui.cs.advprog.listingquery.validation;

import id.ac.ui.cs.advprog.listingquery.dto.ListingCreateRequest;
import id.ac.ui.cs.advprog.listingquery.dto.ListingUpdateRequest;
import id.ac.ui.cs.advprog.listingquery.model.ListingCategory;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListingRequestValidatorTest {

    private final ListingRequestValidator validator = new ListingRequestValidator();

    @Test
    void createRequestShouldRejectBlankTitleAndNonPositivePrice() {
        ListingCreateRequest blankTitle = new ListingCreateRequest(
            " ",
            "Description",
            null,
            BigDecimal.TEN,
            ListingCategory.OTHER
        );
        ListingCreateRequest invalidPrice = new ListingCreateRequest(
            "Title",
            "Description",
            null,
            BigDecimal.ZERO,
            ListingCategory.OTHER
        );

        assertThatThrownBy(() -> validator.validateCreateRequest(blankTitle))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Title is required");
        assertThatThrownBy(() -> validator.validateCreateRequest(invalidPrice))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Price must be positive");
    }

    @Test
    void updateRequestShouldRejectBlankDescription() {
        ListingUpdateRequest request = new ListingUpdateRequest(" ", null, ListingCategory.OTHER);

        assertThatThrownBy(() -> validator.validateUpdateRequest(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Description is required");
    }

    @Test
    void priceRangeShouldRejectMinimumAboveMaximum() {
        assertThatThrownBy(() -> validator.validatePriceRange(BigDecimal.TEN, BigDecimal.ONE))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("minPrice cannot be greater than maxPrice");
    }
}
