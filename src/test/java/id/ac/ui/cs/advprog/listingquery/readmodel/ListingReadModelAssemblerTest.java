package id.ac.ui.cs.advprog.listingquery.readmodel;

import id.ac.ui.cs.advprog.listingquery.lifecycle.ListingLifecyclePolicy;
import id.ac.ui.cs.advprog.listingquery.model.Auction;
import id.ac.ui.cs.advprog.listingquery.model.AuctionStatus;
import id.ac.ui.cs.advprog.listingquery.model.Listing;
import id.ac.ui.cs.advprog.listingquery.model.ListingCategory;
import id.ac.ui.cs.advprog.listingquery.model.ListingStatus;
import id.ac.ui.cs.advprog.listingquery.model.Role;
import id.ac.ui.cs.advprog.listingquery.model.User;
import id.ac.ui.cs.advprog.listingquery.repository.AuctionRepository;
import id.ac.ui.cs.advprog.listingquery.repository.BidRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListingReadModelAssemblerTest {

    private final AuctionRepository auctionRepository = mock(AuctionRepository.class);
    private final BidRepository bidRepository = mock(BidRepository.class);
    private final ListingReadModelAssembler assembler = new ListingReadModelAssembler(
        auctionRepository,
        bidRepository,
        ListingLifecyclePolicy.createDefault(),
        new ListingResponseMapper()
    );

    @Test
    void assembleShouldUseHighestBidAsDisplayPriceWhenAuctionHasBids() {
        Listing listing = listing();
        Auction auction = auction(listing, AuctionStatus.EXTENDED);

        when(auctionRepository.findByListingId(listing.getId())).thenReturn(Optional.of(auction));
        when(bidRepository.countByAuctionId(auction.getId())).thenReturn(2L);
        when(bidRepository.findHighestAmountByAuctionId(auction.getId()))
            .thenReturn(Optional.of(BigDecimal.valueOf(1750)));

        ListingReadModel readModel = assembler.assemble(listing);

        assertThat(readModel.status()).isEqualTo(ListingStatus.EXTENDED);
        assertThat(readModel.displayPrice()).isEqualByComparingTo("1750");
        assertThat(readModel.totalBids()).isEqualTo(2);
        assertThat(readModel.hasBids()).isTrue();
    }

    @Test
    void assembleShouldUseAuctionStartingPriceBeforeAnyBid() {
        Listing listing = listing();
        Auction auction = auction(listing, AuctionStatus.ACTIVE);

        when(auctionRepository.findByListingId(listing.getId())).thenReturn(Optional.of(auction));
        when(bidRepository.countByAuctionId(auction.getId())).thenReturn(0L);
        when(bidRepository.findHighestAmountByAuctionId(auction.getId())).thenReturn(Optional.empty());

        ListingReadModel readModel = assembler.assemble(listing);

        assertThat(readModel.displayPrice()).isEqualByComparingTo("1200");
        assertThat(readModel.hasBids()).isFalse();
    }

    @Test
    void assembleAllShouldBatchAuctionsAndBidSummaries() {
        Listing firstListing = listing();
        Listing secondListing = listing();
        Auction firstAuction = auction(firstListing, AuctionStatus.ACTIVE);
        Auction secondAuction = auction(secondListing, AuctionStatus.WON);
        BidRepository.AuctionBidSummary firstSummary = bidSummary(
            firstAuction.getId(),
            3L,
            BigDecimal.valueOf(1800)
        );

        when(auctionRepository.findByListingIdIn(List.of(firstListing.getId(), secondListing.getId())))
            .thenReturn(List.of(firstAuction, secondAuction));
        when(bidRepository.summarizeByAuctionIds(anyCollection()))
            .thenReturn(List.of(firstSummary));

        List<ListingReadModel> readModels = assembler.assembleAll(List.of(firstListing, secondListing));

        assertThat(readModels).hasSize(2);
        assertThat(readModels.get(0).displayPrice()).isEqualByComparingTo("1800");
        assertThat(readModels.get(0).totalBids()).isEqualTo(3);
        assertThat(readModels.get(1).status()).isEqualTo(ListingStatus.WON);
        assertThat(readModels.get(1).displayPrice()).isEqualByComparingTo("1200");
        assertThat(readModels.get(1).totalBids()).isZero();
    }

    private Listing listing() {
        return Listing.builder()
            .id(UUID.randomUUID())
            .title("Phone")
            .description("Nice phone")
            .price(BigDecimal.valueOf(1000))
            .category(ListingCategory.ELECTRONICS)
            .seller(seller())
            .status(ListingStatus.ACTIVE)
            .createdAt(Instant.parse("2026-05-22T10:00:00Z"))
            .build();
    }

    private Auction auction(Listing listing, AuctionStatus status) {
        Auction auction = new Auction();
        auction.setId(UUID.randomUUID());
        auction.setListing(listing);
        auction.setStatus(status);
        auction.setStartingPrice(BigDecimal.valueOf(1200));
        return auction;
    }

    private User seller() {
        User seller = new User();
        ReflectionTestUtils.setField(seller, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(seller, "email", "seller@example.com");
        ReflectionTestUtils.setField(seller, "role", Role.SELLER);
        return seller;
    }

    private BidRepository.AuctionBidSummary bidSummary(UUID auctionId, long totalBids, BigDecimal highestAmount) {
        return new BidRepository.AuctionBidSummary() {
            @Override
            public UUID getAuctionId() {
                return auctionId;
            }

            @Override
            public long getTotalBids() {
                return totalBids;
            }

            @Override
            public BigDecimal getHighestAmount() {
                return highestAmount;
            }
        };
    }
}
