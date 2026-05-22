package id.ac.ui.cs.advprog.listingquery;

import id.ac.ui.cs.advprog.listingquery.model.ListingCategory;
import id.ac.ui.cs.advprog.listingquery.model.ListingStatus;
import id.ac.ui.cs.advprog.listingquery.service.ListingQueryService;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("profiling")
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:listingqueryprofile;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "security.jwt.secret=test-secret-for-listing-query-12345"
})
@ActiveProfiles("local")
@Transactional
class ListingQueryProfilingTest {

    private static final int LISTING_COUNT = 800;
    private static final int WARMUP_RUNS = 2;
    private static final int MEASURED_RUNS = 8;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ListingQueryService listingQueryService;

    @TempDir
    private Path tempDir;

    private UUID activeListingId;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("delete from bid").executeUpdate();
        entityManager.createNativeQuery("delete from auction").executeUpdate();
        entityManager.createNativeQuery("delete from listing").executeUpdate();
        entityManager.createNativeQuery("delete from app_user").executeUpdate();

        UUID sellerId = insertUser("seller-profile@example.com", "SELLER");
        insertUser("buyer-profile@example.com", "BUYER");
        seedListings(sellerId);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void profileCriticalListingQueryFunctions() throws IOException {
        List<ProfileResult> results = List.of(
            profile("get-all-active-listings", () -> listingQueryService.getAllListings(
                PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt")),
                null,
                null,
                null,
                null,
                ListingStatus.ACTIVE,
                null,
                null
            )),
            profile("search-filter-listings", () -> listingQueryService.getAllListings(
                PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt")),
                ListingCategory.ELECTRONICS,
                "phone",
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                ListingStatus.ACTIVE,
                null,
                null
            )),
            profile("get-listing-detail-by-id", () -> listingQueryService.getListingDetail(activeListingId)),
            profile("validate-listing-can-receive-bid", () -> listingQueryService.validateListingForBid(activeListingId))
        );

        writeProfilingCsv(results);
        assertThat(results).allSatisfy(result -> assertThat(result.averageMillis()).isPositive());
    }

    private ProfileResult profile(String name, Runnable action) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            action.run();
        }

        List<Long> durationsNanos = new ArrayList<>();
        for (int i = 0; i < MEASURED_RUNS; i++) {
            long startedAt = System.nanoTime();
            action.run();
            durationsNanos.add(System.nanoTime() - startedAt);
        }

        LongSummaryStatistics statistics = durationsNanos.stream()
            .mapToLong(Long::longValue)
            .summaryStatistics();

        return new ProfileResult(
            name,
            Duration.ofNanos((long) statistics.getAverage()).toNanos() / 1_000_000.0,
            Duration.ofNanos(statistics.getMin()).toNanos() / 1_000_000.0,
            Duration.ofNanos(statistics.getMax()).toNanos() / 1_000_000.0,
            nanosToMillis(percentile(durationsNanos, 95)),
            nanosToMillis(percentile(durationsNanos, 99)),
            throughputPerSecond(durationsNanos),
            0.0
        );
    }

    private void writeProfilingCsv(List<ProfileResult> results) throws IOException {
        String label = System.getProperty("profiling.label", "baseline");
        Path reportDir = Path.of("build", "reports", "profiling");
        Files.createDirectories(reportDir);
        Path reportPath = reportDir.resolve("listing-query-" + label + ".csv");

        List<String> lines = new ArrayList<>();
        lines.add("function,average_ms,min_ms,max_ms,p95_ms,p99_ms,throughput_ops_sec,error_rate,dataset_size,warmup_runs,measured_runs");
        results.forEach(result -> lines.add(String.format(
            java.util.Locale.ROOT,
            "%s,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%d,%d,%d",
            result.functionName(),
            result.averageMillis(),
            result.minMillis(),
            result.maxMillis(),
            result.p95Millis(),
            result.p99Millis(),
            result.throughputOpsPerSecond(),
            result.errorRate(),
            LISTING_COUNT,
            WARMUP_RUNS,
            MEASURED_RUNS
        )));
        Files.write(reportPath, lines);

        Path mirrorPath = tempDir.resolve(reportPath.getFileName());
        Files.write(mirrorPath, lines);
    }

    private double nanosToMillis(long nanos) {
        return Duration.ofNanos(nanos).toNanos() / 1_000_000.0;
    }

    private long percentile(List<Long> durationsNanos, int percentile) {
        List<Long> sortedDurations = new ArrayList<>(durationsNanos);
        Collections.sort(sortedDurations);
        int index = (int) Math.ceil(percentile / 100.0 * sortedDurations.size()) - 1;
        return sortedDurations.get(Math.max(0, Math.min(index, sortedDurations.size() - 1)));
    }

    private double throughputPerSecond(List<Long> durationsNanos) {
        long totalNanos = durationsNanos.stream()
            .mapToLong(Long::longValue)
            .sum();
        return durationsNanos.size() / (totalNanos / 1_000_000_000.0);
    }

    private void seedListings(UUID sellerId) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        for (int index = 0; index < LISTING_COUNT; index++) {
            UUID listingId = UUID.randomUUID();
            UUID auctionId = UUID.randomUUID();
            String status = statusFor(index);
            BigDecimal price = BigDecimal.valueOf(1000 + (index % 100));
            if (index == 0) {
                activeListingId = listingId;
                status = "ACTIVE";
            }

            entityManager.createNativeQuery("""
                insert into listing (
                    id, title, description, image_url, price, category, seller_id, status,
                    created_at, updated_at, cancelled_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, null, null)
                """)
                .setParameter(1, listingId.toString())
                .setParameter(2, "Phone " + index)
                .setParameter(3, "Profiled phone listing " + index)
                .setParameter(4, "https://img.example/phone-" + index + ".jpg")
                .setParameter(5, price)
                .setParameter(6, index % 2 == 0 ? "ELECTRONICS" : "FASHION")
                .setParameter(7, sellerId.toString())
                .setParameter(8, status)
                .setParameter(9, now.minus(index, ChronoUnit.MINUTES))
                .executeUpdate();

            entityManager.createNativeQuery("""
                insert into auction (
                    id, listing_id, status, starting_price, reserve_price, minimum_bid_increment,
                    duration_minutes, created_at, starts_at, ends_at, closed_at
                ) values (?, ?, ?, ?, ?, 10.00, 120, ?, ?, ?, null)
                """)
                .setParameter(1, auctionId.toString())
                .setParameter(2, listingId.toString())
                .setParameter(3, status)
                .setParameter(4, price)
                .setParameter(5, price.add(BigDecimal.valueOf(100)))
                .setParameter(6, now.minus(index, ChronoUnit.MINUTES))
                .setParameter(7, "DRAFT".equals(status) ? null : now.minus(index, ChronoUnit.MINUTES))
                .setParameter(8, "DRAFT".equals(status) ? null : now.plus(2, ChronoUnit.HOURS))
                .executeUpdate();

            if (index % 3 == 0) {
                insertBid(auctionId, price.add(BigDecimal.valueOf(50)));
            }
        }
    }

    private UUID insertUser(String email, String role) {
        UUID id = UUID.randomUUID();
        entityManager.createNativeQuery("""
            insert into app_user (id, email, role)
            values (?, ?, ?)
            """)
            .setParameter(1, id.toString())
            .setParameter(2, email)
            .setParameter(3, role)
            .executeUpdate();
        return id;
    }

    private void insertBid(UUID auctionId, BigDecimal amount) {
        entityManager.createNativeQuery("""
            insert into bid (id, auction_id, amount)
            values (?, ?, ?)
            """)
            .setParameter(1, UUID.randomUUID().toString())
            .setParameter(2, auctionId.toString())
            .setParameter(3, amount)
            .executeUpdate();
    }

    private String statusFor(int index) {
        return switch (index % 6) {
            case 0 -> "ACTIVE";
            case 1 -> "EXTENDED";
            case 2 -> "DRAFT";
            case 3 -> "WON";
            case 4 -> "UNSOLD";
            default -> "CANCELLED";
        };
    }

    private record ProfileResult(
        String functionName,
        double averageMillis,
        double minMillis,
        double maxMillis,
        double p95Millis,
        double p99Millis,
        double throughputOpsPerSecond,
        double errorRate
    ) {
    }
}
