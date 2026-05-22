package id.ac.ui.cs.advprog.listingquery.filter;

import id.ac.ui.cs.advprog.listingquery.model.Listing;
import org.springframework.data.jpa.domain.Specification;

public interface ListingSpecificationStrategy {

    Specification<Listing> toSpecification(ListingFilterCriteria criteria);
}
