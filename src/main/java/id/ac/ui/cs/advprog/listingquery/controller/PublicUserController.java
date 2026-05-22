package id.ac.ui.cs.advprog.listingquery.controller;

import id.ac.ui.cs.advprog.listingquery.dto.PublicSellerProfileResponse;
import id.ac.ui.cs.advprog.listingquery.service.ListingQueryService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class PublicUserController {

    private final ListingQueryService listingQueryService;

    public PublicUserController(ListingQueryService listingQueryService) {
        this.listingQueryService = listingQueryService;
    }

    @GetMapping("/{userId}/public-profile")
    public PublicSellerProfileResponse publicProfile(@PathVariable UUID userId) {
        return listingQueryService.getPublicSellerProfile(userId);
    }
}
