# Profiling Optimization Report

## Optimized Function

Primary optimization target:

- `get all active listings`
- `search/filter listings`

Reason:

- These are catalog paths likely called frequently by buyers.
- Baseline profiling showed these were much slower than detail/validation.

## Optimization

Implemented batch read model assembly:

- `AuctionRepository.findByListingIdIn(...)`
- `BidRepository.summarizeByAuctionIds(...)`
- `ListingReadModelAssembler.assembleAll(...)`
- `ListingQueryService.getAllListings(...)` now filters and maps from batch-assembled read models.

This removes per-listing auction/bid lookup on list/search paths.

## Baseline vs Optimized

| Function | Baseline avg ms | Optimized avg ms | Improvement |
| --- | ---: | ---: | ---: |
| get-all-active-listings | 646.756 | 23.064 | 96.43% |
| search-filter-listings | 328.094 | 14.420 | 95.61% |
| get-listing-detail-by-id | 1.809 | 4.166 | -130.29% |
| validate-listing-can-receive-bid | 0.574 | 1.740 | -203.14% |

## Evidence Commands

Baseline:

```powershell
.\gradlew "-Dprofiling.label=baseline" profilingTest
```

Optimized:

```powershell
.\gradlew "-Dprofiling.label=optimized" profilingTest --rerun-tasks
```

## Interpretation

- The 50% improvement target is achieved and exceeded for the optimized catalog list/search functions.
- Detail and validation were not optimized in this change and showed slower numbers in the later run. Because those paths are sub-5ms in this synthetic benchmark, this may be measurement noise or JVM/test-context variance, but no improvement is claimed for them.
- The claim of >=50% improvement applies only to `get-all-active-listings` and `search-filter-listings`.

## Limitations

- Simple timing benchmark, not JMH/JMeter/k6.
- H2 in-memory database, not production PostgreSQL.
- Results should be validated again with production-like data and PostgreSQL before making capacity claims.
