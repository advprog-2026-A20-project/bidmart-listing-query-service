package id.ac.ui.cs.advprog.listingquery.filter;

import id.ac.ui.cs.advprog.listingquery.model.Listing;
import id.ac.ui.cs.advprog.listingquery.model.ListingCategory;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

class ListingSpecificationBuilderTest {

    @Test
    void buildShouldCombineOnlyStrategiesThatReturnSpecification() {
        AtomicInteger calledStrategies = new AtomicInteger();
        ListingSpecificationStrategy emptyStrategy = criteria -> {
            calledStrategies.incrementAndGet();
            return null;
        };
        ListingSpecificationStrategy matchingStrategy = criteria -> {
            calledStrategies.incrementAndGet();
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        };

        ListingSpecificationBuilder builder = new ListingSpecificationBuilder(
            List.of(emptyStrategy, matchingStrategy)
        );

        Specification<Listing> specification = builder.build(new ListingFilterCriteria(
            ListingCategory.ELECTRONICS,
            "phone",
            BigDecimal.ONE,
            BigDecimal.TEN
        ));

        assertThat(specification).isNotNull();
        assertThat(calledStrategies).hasValue(2);
    }

    @Test
    void blankKeywordShouldNotCreateSpecification() {
        KeywordListingSpecificationStrategy strategy = new KeywordListingSpecificationStrategy();

        Specification<Listing> specification = strategy.toSpecification(new ListingFilterCriteria(
            null,
            " ",
            null,
            null
        ));

        assertThat(specification).isNull();
    }
}
