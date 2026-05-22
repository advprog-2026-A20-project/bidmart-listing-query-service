package id.ac.ui.cs.advprog.listingquery.pagination;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ListingPageRequestPolicy {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 50;

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
        "createdAt",
        "updatedAt",
        "price",
        "title",
        "category",
        "status"
    );

    public Pageable sanitize(Pageable pageable) {
        int requestedPageSize = pageable.isPaged() ? pageable.getPageSize() : DEFAULT_PAGE_SIZE;
        int safePageNumber = pageable.isPaged() ? Math.max(pageable.getPageNumber(), 0) : 0;
        Sort safeSort = sanitizeSort(pageable.getSort());

        return PageRequest.of(
            safePageNumber,
            Math.max(1, Math.min(requestedPageSize, MAX_PAGE_SIZE)),
            safeSort
        );
    }

    private Sort sanitizeSort(Sort requestedSort) {
        if (requestedSort == null || requestedSort.isUnsorted()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        requestedSort.forEach(order -> {
            if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported sort field: " + order.getProperty()
                );
            }
        });

        return requestedSort;
    }
}
