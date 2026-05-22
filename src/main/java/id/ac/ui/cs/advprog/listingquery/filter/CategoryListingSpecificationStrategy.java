package id.ac.ui.cs.advprog.listingquery.filter;

import id.ac.ui.cs.advprog.listingquery.model.Listing;
import id.ac.ui.cs.advprog.listingquery.model.ListingCategory;
import java.util.Arrays;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class CategoryListingSpecificationStrategy implements ListingSpecificationStrategy {

    @Override
    public Specification<Listing> toSpecification(ListingFilterCriteria criteria) {
        if (criteria.category() == null) {
            return null;
        }

        List<ListingCategory> matchingCategories = Arrays.stream(ListingCategory.values())
            .filter(candidate -> candidate.isSameOrDescendantOf(criteria.category()))
            .toList();

        return (root, query, criteriaBuilder) -> root.get("category").in(matchingCategories);
    }
}
