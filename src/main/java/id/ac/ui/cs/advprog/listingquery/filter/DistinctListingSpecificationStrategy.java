package id.ac.ui.cs.advprog.listingquery.filter;

import id.ac.ui.cs.advprog.listingquery.model.Listing;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class DistinctListingSpecificationStrategy implements ListingSpecificationStrategy {

    @Override
    public Specification<Listing> toSpecification(ListingFilterCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            return criteriaBuilder.conjunction();
        };
    }
}
