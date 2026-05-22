package id.ac.ui.cs.advprog.listingquery.filter;

import id.ac.ui.cs.advprog.listingquery.model.Listing;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@Order(30)
public class PriceRangeListingSpecificationStrategy implements ListingSpecificationStrategy {

    @Override
    public Specification<Listing> toSpecification(ListingFilterCriteria criteria) {
        Specification<Listing> specification = null;

        if (criteria.minPrice() != null) {
            specification = combine(
                specification,
                (root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("price"), criteria.minPrice())
            );
        }
        if (criteria.maxPrice() != null) {
            specification = combine(
                specification,
                (root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("price"), criteria.maxPrice())
            );
        }

        return specification;
    }

    private Specification<Listing> combine(
        Specification<Listing> base,
        Specification<Listing> next
    ) {
        return base == null ? next : base.and(next);
    }
}
