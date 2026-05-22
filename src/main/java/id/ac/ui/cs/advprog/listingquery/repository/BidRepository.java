package id.ac.ui.cs.advprog.listingquery.repository;

import id.ac.ui.cs.advprog.listingquery.model.Bid;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BidRepository extends JpaRepository<Bid, UUID> {

    interface AuctionBidSummary {
        UUID getAuctionId();
        long getTotalBids();
        BigDecimal getHighestAmount();
    }

    @Query("""
        select count(b)
        from Bid b
        where b.auction.id = :auctionId
        """)
    long countByAuctionId(@Param("auctionId") UUID auctionId);

    @Query("""
        select max(b.amount)
        from Bid b
        where b.auction.id = :auctionId
        """)
    Optional<BigDecimal> findHighestAmountByAuctionId(@Param("auctionId") UUID auctionId);

    @Query("""
        select b.auction.id as auctionId,
               count(b) as totalBids,
               max(b.amount) as highestAmount
        from Bid b
        where b.auction.id in :auctionIds
        group by b.auction.id
        """)
    List<AuctionBidSummary> summarizeByAuctionIds(@Param("auctionIds") Collection<UUID> auctionIds);

    @Query("""
        select count(b) > 0
        from Bid b
        where b.auction.listing.id = :listingId
        """)
    boolean existsByListingId(@Param("listingId") UUID listingId);
}
