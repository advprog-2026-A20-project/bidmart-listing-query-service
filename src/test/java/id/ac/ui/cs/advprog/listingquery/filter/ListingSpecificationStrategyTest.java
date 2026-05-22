package id.ac.ui.cs.advprog.listingquery.filter;

import id.ac.ui.cs.advprog.listingquery.model.ListingCategory;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ListingSpecificationStrategyTest {

    @Test
    void categoryStrategyShouldSkipNullCategoryAndAcceptCategoryFilter() {
        CategoryListingSpecificationStrategy strategy = new CategoryListingSpecificationStrategy();

        assertThat(strategy.toSpecification(new ListingFilterCriteria(null, null, null, null))).isNull();
        assertThat(strategy.toSpecification(new ListingFilterCriteria(
            ListingCategory.ELECTRONICS,
            null,
            null,
            null
        ))).isNotNull();
    }

    @Test
    void keywordStrategyShouldSkipBlankKeywordAndAcceptTextSearch() {
        KeywordListingSpecificationStrategy strategy = new KeywordListingSpecificationStrategy();

        assertThat(strategy.toSpecification(new ListingFilterCriteria(null, " ", null, null))).isNull();
        assertThat(strategy.toSpecification(new ListingFilterCriteria(null, "phone", null, null))).isNotNull();
    }

    @Test
    void priceRangeStrategyShouldSkipEmptyRangeAndAcceptBounds() {
        PriceRangeListingSpecificationStrategy strategy = new PriceRangeListingSpecificationStrategy();

        assertThat(strategy.toSpecification(new ListingFilterCriteria(null, null, null, null))).isNull();
        assertThat(strategy.toSpecification(new ListingFilterCriteria(
            null,
            null,
            BigDecimal.ONE,
            BigDecimal.TEN
        ))).isNotNull();
    }
}
