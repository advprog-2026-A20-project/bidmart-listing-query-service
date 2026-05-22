package id.ac.ui.cs.advprog.listingquery.filter;

import id.ac.ui.cs.advprog.listingquery.model.ListingCategory;
import java.math.BigDecimal;

public record ListingFilterCriteria(
    ListingCategory category,
    String keyword,
    BigDecimal minPrice,
    BigDecimal maxPrice
) {
}
