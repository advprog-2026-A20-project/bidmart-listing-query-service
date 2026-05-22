package id.ac.ui.cs.advprog.listingquery.architecture;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:listingarchtest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "security.jwt.secret=test-secret-for-listing-query-12345"
})
@ActiveProfiles("local")
class ListingServiceArchitectureTest {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    void listingServiceShouldNotExposeBiddingWalletOrAuthCommandBoundaries() {
        Set<String> paths = handlerMapping.getHandlerMethods().keySet().stream()
            .map(RequestMappingInfo::toString)
            .collect(java.util.stream.Collectors.toSet());

        assertThat(paths).noneMatch(path -> path.contains("/api/bids"));
        assertThat(paths).noneMatch(path -> path.contains("/api/wallet"));
        assertThat(paths).noneMatch(path -> path.contains("/api/auth"));
        assertThat(paths).noneMatch(path -> path.contains("/login"));
        assertThat(paths).noneMatch(path -> path.contains("/register"));
    }

    @Test
    void listingApiBoundaryShouldExposeExpectedListingEndpoints() {
        String joinedMappings = handlerMapping.getHandlerMethods().keySet().toString();

        assertThat(joinedMappings).contains("/api/listings");
        assertThat(joinedMappings).contains("/api/listings/{listingId}");
        assertThat(joinedMappings).contains("/api/listings/{listingId}/validation");
        assertThat(joinedMappings).contains("/api/listings/categories");
        assertThat(joinedMappings).contains("/api/listings/categories/tree");
    }
}
