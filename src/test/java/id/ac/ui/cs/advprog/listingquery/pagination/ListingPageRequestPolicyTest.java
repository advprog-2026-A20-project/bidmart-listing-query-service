package id.ac.ui.cs.advprog.listingquery.pagination;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListingPageRequestPolicyTest {

    private final ListingPageRequestPolicy policy = new ListingPageRequestPolicy();

    @Test
    void sanitizeShouldClampPageSizeAndApplyDefaultSort() {
        Pageable sanitized = policy.sanitize(PageRequest.of(0, 999));

        assertThat(sanitized.getPageSize()).isEqualTo(ListingPageRequestPolicy.MAX_PAGE_SIZE);
        assertThat(sanitized.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(sanitized.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void sanitizeShouldKeepAllowedSortField() {
        Pageable sanitized = policy.sanitize(PageRequest.of(0, 10, Sort.by("price").ascending()));

        assertThat(sanitized.getSort().getOrderFor("price")).isNotNull();
        assertThat(sanitized.getSort().getOrderFor("price").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void sanitizeShouldRejectUnsupportedSortField() {
        Pageable request = PageRequest.of(0, 10, Sort.by("seller.password").ascending());

        assertThatThrownBy(() -> policy.sanitize(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Unsupported sort field");
    }
}
