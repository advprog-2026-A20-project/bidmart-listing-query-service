package id.ac.ui.cs.advprog.listingquery.controller;

import id.ac.ui.cs.advprog.listingquery.dto.ListingCategoryNodeResponse;
import id.ac.ui.cs.advprog.listingquery.dto.ListingBidValidationResponse;
import id.ac.ui.cs.advprog.listingquery.dto.ListingCreateRequest;
import id.ac.ui.cs.advprog.listingquery.dto.ListingDetailResponse;
import id.ac.ui.cs.advprog.listingquery.dto.ListingResponse;
import id.ac.ui.cs.advprog.listingquery.dto.ListingUpdateRequest;
import id.ac.ui.cs.advprog.listingquery.model.ListingCategory;
import id.ac.ui.cs.advprog.listingquery.model.ListingStatus;
import id.ac.ui.cs.advprog.listingquery.security.AuthenticatedUser;
import id.ac.ui.cs.advprog.listingquery.service.ListingQueryService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/listings")
public class ListingQueryController {

    private final ListingQueryService listingQueryService;

    public ListingQueryController(ListingQueryService listingQueryService) {
        this.listingQueryService = listingQueryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SELLER')")
    public ListingResponse create(
        @Valid @RequestBody ListingCreateRequest request,
        @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return listingQueryService.createListing(request, authenticatedUser.id());
    }

    @GetMapping
    public List<ListingResponse> list(
        Pageable pageable,
        @RequestParam(required = false) ListingCategory category,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) ListingStatus status,
        @RequestParam(required = false) Instant endingAfter,
        @RequestParam(required = false) Instant endingBefore
    ) {
        return listingQueryService.getAllListings(
            pageable,
            category,
            keyword,
            minPrice,
            maxPrice,
            status,
            endingAfter,
            endingBefore
        );
    }

    @GetMapping("/{listingId}")
    public ListingDetailResponse getById(@PathVariable UUID listingId) {
        return listingQueryService.getListingDetail(listingId);
    }

    @GetMapping("/categories")
    public List<ListingCategory> categories() {
        return listingQueryService.getCategories();
    }

    @GetMapping("/categories/tree")
    public List<ListingCategoryNodeResponse> categoryTree() {
        return listingQueryService.getCategoryTree();
    }

    @GetMapping("/{listingId}/validation")
    public ListingBidValidationResponse validateForBid(@PathVariable UUID listingId) {
        return listingQueryService.validateListingForBid(listingId);
    }

    @PutMapping("/{listingId}")
    @PreAuthorize("hasRole('SELLER')")
    public ListingDetailResponse update(
        @PathVariable UUID listingId,
        @Valid @RequestBody ListingUpdateRequest request,
        @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return listingQueryService.updateListing(listingId, request, authenticatedUser.id());
    }

    @DeleteMapping("/{listingId}")
    @PreAuthorize("hasRole('SELLER')")
    public ListingDetailResponse cancel(
        @PathVariable UUID listingId,
        @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return listingQueryService.cancelListing(listingId, authenticatedUser.id());
    }
}
