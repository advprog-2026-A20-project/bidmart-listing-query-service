package id.ac.ui.cs.advprog.listingquery.filter;

import id.ac.ui.cs.advprog.listingquery.model.Listing;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ListingSpecificationBuilder {

    private final List<ListingSpecificationStrategy> strategies;

    public ListingSpecificationBuilder(List<ListingSpecificationStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public Specification<Listing> build(ListingFilterCriteria criteria) {
        Specification<Listing> specification = null;

        for (ListingSpecificationStrategy strategy : strategies) {
            Specification<Listing> nextSpecification = strategy.toSpecification(criteria);
            if (nextSpecification != null) {
                specification = specification == null
                    ? nextSpecification
                    : specification.and(nextSpecification);
            }
        }

        return specification;
    }
}
