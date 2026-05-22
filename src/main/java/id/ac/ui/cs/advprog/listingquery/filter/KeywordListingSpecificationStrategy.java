package id.ac.ui.cs.advprog.listingquery.filter;

import id.ac.ui.cs.advprog.listingquery.model.Listing;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class KeywordListingSpecificationStrategy implements ListingSpecificationStrategy {

    @Override
    public Specification<Listing> toSpecification(ListingFilterCriteria criteria) {
        if (criteria.keyword() == null || criteria.keyword().isBlank()) {
            return null;
        }

        String normalizedKeyword = "%" + criteria.keyword().trim().toLowerCase() + "%";
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), normalizedKeyword),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), normalizedKeyword)
        );
    }
}
