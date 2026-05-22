package id.ac.ui.cs.advprog.listingquery;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import id.ac.ui.cs.advprog.listingquery.service.ListingQueryService;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:listingquerytest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "security.jwt.secret=test-secret-for-listing-query-12345"
})
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class ListingQueryIntegrationTest {

    private static final String JWT_SECRET = "test-secret-for-listing-query-12345";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ListingQueryService listingQueryService;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("delete from bid").executeUpdate();
        entityManager.createNativeQuery("delete from auction").executeUpdate();
        entityManager.createNativeQuery("delete from listing").executeUpdate();
        entityManager.createNativeQuery("delete from app_user").executeUpdate();
    }

    @Test
    void healthcheckShouldBeAvailable() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void readEndpointsShouldReturnListingSummariesDetailsAndCategories() throws Exception {
        String sellerId = insertUser("seller@example.com");
        insertUser("buyer@example.com");
        String listingId = insertListing(sellerId);
        String auctionId = insertAuction(listingId);
        insertBid(auctionId);

        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(listingId))
            .andExpect(jsonPath("$[0].sellerEmail").value("seller@example.com"))
            .andExpect(jsonPath("$[0].auctionId").value(auctionId))
            .andExpect(jsonPath("$[0].totalBids").value(1));

        mockMvc.perform(get("/api/listings/{listingId}", listingId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(listingId))
            .andExpect(jsonPath("$.category").value("ELECTRONICS"))
            .andExpect(jsonPath("$.auctionId").value(auctionId))
            .andExpect(jsonPath("$.hasBids").value(true));

        mockMvc.perform(get("/api/listings/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").value("ELECTRONICS"));

        mockMvc.perform(get("/api/listings/categories/tree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].key").value("ELECTRONICS"))
            .andExpect(jsonPath("$[0].children[0].key").value("ELECTRONICS_PHONE"));
    }

    @Test
    void cancelDraftListingShouldCancelListingAndAuction() {
        String sellerId = insertUser("seller@example.com");
        String listingId = insertListing(sellerId);
        String auctionId = insertAuction(listingId, "DRAFT");

        listingQueryService.cancelListing(UUID.fromString(listingId), UUID.fromString(sellerId));
        entityManager.flush();

        Object listingStatus = entityManager.createNativeQuery("select status from listing where id = ?")
            .setParameter(1, listingId)
            .getSingleResult();
        Object auctionStatus = entityManager.createNativeQuery("select status from auction where id = ?")
            .setParameter(1, auctionId)
            .getSingleResult();

        org.assertj.core.api.Assertions.assertThat(listingStatus).isEqualTo("CANCELLED");
        org.assertj.core.api.Assertions.assertThat(auctionStatus).isEqualTo("CANCELLED");
    }

    @Test
    void cancelledListingDetailShouldRemainVisible() throws Exception {
        String sellerId = insertUser("seller@example.com");
        String listingId = insertListing(sellerId, "CANCELLED", "1200.00");
        String auctionId = insertAuction(listingId, "CANCELLED");

        mockMvc.perform(get("/api/listings/{listingId}", listingId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(listingId))
            .andExpect(jsonPath("$.status").value("CANCELLED"))
            .andExpect(jsonPath("$.auctionId").value(auctionId))
            .andExpect(jsonPath("$.auctionStatus").value("CANCELLED"));
    }

    @Test
    void listingSummaryAndDetailPriceShouldFollowHighestAuctionBid() throws Exception {
        String sellerId = insertUser("seller@example.com");
        String listingId = insertListing(sellerId, "ACTIVE", "1200.00");
        String auctionId = insertAuction(listingId, "ACTIVE");
        insertBid(auctionId, "1250.00");
        insertBid(auctionId, "1500.00");

        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(listingId))
            .andExpect(jsonPath("$[0].price").value(1500.00));

        mockMvc.perform(get("/api/listings/{listingId}", listingId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(listingId))
            .andExpect(jsonPath("$.price").value(1500.00))
            .andExpect(jsonPath("$.startingPrice").value(1200.00));
    }

    @Test
    void listingPriceShouldUseAuctionStartingPriceBeforeAnyBid() throws Exception {
        String sellerId = insertUser("seller@example.com");
        String listingId = insertListing(sellerId, "ACTIVE", "1000.00");
        insertAuction(listingId, "ACTIVE", "1200.00");

        mockMvc.perform(get("/api/listings/{listingId}", listingId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.price").value(1200.00))
            .andExpect(jsonPath("$.startingPrice").value(1200.00));
    }

    @Test
    void statusFilterAndBidValidationShouldUseEffectiveAuctionStatus() throws Exception {
        String sellerId = insertUser("seller@example.com");
        String activeListingId = insertListing(sellerId, "ACTIVE", "1000.00");
        String wonListingId = insertListing(sellerId, "ACTIVE", "1000.00");
        insertAuction(activeListingId, "ACTIVE", "1000.00");
        insertAuction(wonListingId, "WON", "1000.00");

        mockMvc.perform(get("/api/listings").param("status", "WON"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(wonListingId))
            .andExpect(jsonPath("$[0].status").value("WON"));

        mockMvc.perform(get("/api/listings/{listingId}/validation", wonListingId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false))
            .andExpect(jsonPath("$.biddable").value(false))
            .andExpect(jsonPath("$.listingStatus").value("WON"));
    }

    @Test
    void createListingShouldRequireSellerAuthentication() throws Exception {
        String sellerId = insertUser("seller@example.com");
        String createBody = """
            {
              "title": "Desk Setup",
              "description": "Standing desk",
              "imageUrl": "https://img.example/desk.jpg",
              "price": 750.00,
              "category": "HOME_LIVING_FURNITURE"
            }
            """;

        mockMvc.perform(post("/api/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/listings")
                .header("Authorization", "Bearer " + jwtFor(sellerId, "seller@example.com", "SELLER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Desk Setup"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.sellerEmail").value("seller@example.com"));
    }

    @Test
    void invalidCreateRequestShouldReturnBadRequestWithoutStackTrace() throws Exception {
        String sellerId = insertUser("seller@example.com");

        mockMvc.perform(post("/api/listings")
                .header("Authorization", "Bearer " + jwtFor(sellerId, "seller@example.com", "SELLER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "",
                      "description": "Invalid",
                      "price": 0
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void updateListingWithExistingBidShouldBeRejected() throws Exception {
        String sellerId = insertUser("seller@example.com");
        String listingId = insertListing(sellerId, "ACTIVE", "1000.00");
        String auctionId = insertAuction(listingId, "ACTIVE", "1000.00");
        insertBid(auctionId, "1100.00");

        mockMvc.perform(put("/api/listings/{listingId}", listingId)
                .header("Authorization", "Bearer " + jwtFor(sellerId, "seller@example.com", "SELLER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "description": "Updated description",
                      "imageUrl": "https://img.example/new.jpg",
                      "category": "ELECTRONICS"
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Listing cannot be modified because it already has bids"));
    }

    @Test
    void publicSellerProfileShouldExposeListingAndAuctionCounts() throws Exception {
        String sellerId = insertUser("seller@example.com");
        String activeListingId = insertListing(sellerId, "ACTIVE", "1000.00");
        String wonListingId = insertListing(sellerId, "ACTIVE", "1000.00");
        insertAuction(activeListingId, "ACTIVE", "1000.00");
        insertAuction(wonListingId, "WON", "1000.00");

        mockMvc.perform(get("/api/users/{userId}/public-profile", sellerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(sellerId))
            .andExpect(jsonPath("$.email").value("seller@example.com"))
            .andExpect(jsonPath("$.role").value("SELLER"))
            .andExpect(jsonPath("$.activeListingCount").value(2))
            .andExpect(jsonPath("$.liveAuctionCount").value(1))
            .andExpect(jsonPath("$.completedAuctionCount").value(1));
    }

    @Test
    void listEndpointShouldClampMaxPageSize() throws Exception {
        String sellerId = insertUser("seller@example.com");
        for (int index = 0; index < 60; index++) {
            insertListing(sellerId, "ACTIVE", "1000.00");
        }

        mockMvc.perform(get("/api/listings").param("size", "999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(50));
    }

    @Test
    void listEndpointShouldRejectUnsupportedSortField() throws Exception {
        mockMvc.perform(get("/api/listings").param("sort", "seller.password,asc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Unsupported sort field: seller.password"));
    }

    @Test
    void createListingShouldIgnoreMassAssignedStatusAndSellerIdFields() throws Exception {
        String sellerId = insertUser("seller@example.com");
        String attackerSellerId = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/listings")
                .header("Authorization", "Bearer " + jwtFor(sellerId, "seller@example.com", "SELLER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Safe Listing",
                      "description": "Mass assignment attempt",
                      "imageUrl": "https://img.example/safe.jpg",
                      "price": 1200.00,
                      "category": "ELECTRONICS",
                      "status": "CANCELLED",
                      "sellerId": "%s",
                      "currentPrice": 1.00,
                      "bidCount": 999,
                      "winnerId": "%s"
                    }
                    """.formatted(attackerSellerId, UUID.randomUUID())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.sellerId").value(sellerId))
            .andExpect(jsonPath("$.price").value(1200.00));
    }

    private String insertUser(String email) {
        String id = UUID.randomUUID().toString();
        entityManager.createNativeQuery("""
            insert into app_user (id, email, role)
            values (?, ?, ?)
            """)
            .setParameter(1, id)
            .setParameter(2, email)
            .setParameter(3, email.startsWith("seller") ? "SELLER" : "BUYER")
            .executeUpdate();
        return id;
    }

    private String insertListing(String sellerId) {
        return insertListing(sellerId, "ACTIVE", "1200.00");
    }

    private String insertListing(String sellerId, String status, String price) {
        String id = UUID.randomUUID().toString();
        entityManager.createNativeQuery("""
            insert into listing (id, title, description, image_url, price, category, seller_id, status, created_at, updated_at, cancelled_at)
            values (?, 'Gaming Phone', 'Competitive smartphone', 'https://img.example/phone.jpg', ?, 'ELECTRONICS', ?, ?, ?, null, null)
            """)
            .setParameter(1, id)
            .setParameter(2, new java.math.BigDecimal(price))
            .setParameter(3, sellerId)
            .setParameter(4, status)
            .setParameter(5, Instant.now().truncatedTo(ChronoUnit.SECONDS))
            .executeUpdate();
        return id;
    }

    private String insertAuction(String listingId) {
        return insertAuction(listingId, "ACTIVE");
    }

    private String insertAuction(String listingId, String status) {
        return insertAuction(listingId, status, "1200.00");
    }

    private String insertAuction(String listingId, String status, String startingPrice) {
        String id = UUID.randomUUID().toString();
        Instant createdAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        entityManager.createNativeQuery("""
            insert into auction (
                id, listing_id, status, starting_price, reserve_price, minimum_bid_increment,
                duration_minutes, created_at, starts_at, ends_at, closed_at
            ) values (?, ?, ?, ?, 1200.00, 10.00, 60, ?, ?, ?, null)
            """)
            .setParameter(1, id)
            .setParameter(2, listingId)
            .setParameter(3, status)
            .setParameter(4, new java.math.BigDecimal(startingPrice))
            .setParameter(5, createdAt)
            .setParameter(6, "DRAFT".equals(status) ? null : createdAt)
            .setParameter(7, "DRAFT".equals(status) ? null : createdAt.plus(2, ChronoUnit.HOURS))
            .executeUpdate();
        return id;
    }

    private void insertBid(String auctionId) {
        insertBid(auctionId, "1250.00");
    }

    private void insertBid(String auctionId, String amount) {
        entityManager.createNativeQuery("""
            insert into bid (id, auction_id, amount)
            values (?, ?, ?)
            """)
            .setParameter(1, UUID.randomUUID().toString())
            .setParameter(2, auctionId)
            .setParameter(3, new java.math.BigDecimal(amount))
            .executeUpdate();
    }

    private String jwtFor(String userId, String email, String role) {
        return Jwts.builder()
            .setSubject(userId)
            .claim("email", email)
            .claim("role", role)
            .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
            .compact();
    }
}
