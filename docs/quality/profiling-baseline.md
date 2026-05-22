# Profiling Baseline

## Command

```powershell
.\gradlew "-Dprofiling.label=baseline" profilingTest
```

## Dataset

- H2 in-memory database
- 800 listings
- mixed statuses: ACTIVE, EXTENDED, DRAFT, WON, UNSOLD, CANCELLED
- some listings have bids
- warmup runs: 2
- measured runs: 8

## Baseline Results

| Function | Average ms | Min ms | Max ms |
| --- | ---: | ---: | ---: |
| get-all-active-listings | 646.756 | 545.011 | 773.436 |
| search-filter-listings | 328.094 | 281.033 | 384.480 |
| get-listing-detail-by-id | 1.809 | 1.575 | 2.132 |
| validate-listing-can-receive-bid | 0.574 | 0.501 | 0.649 |

## Bottleneck

The catalog list/search path did repeated auction and bid lookups per listing while filtering and assembling response DTOs. This is the main N+1-style bottleneck found by the baseline.
