# Listing Load Testing Report

## 1. Tujuan Load Test

Membuktikan manfaat Query Optimization Architecture pada endpoint/fungsi read-heavy listing-query-service.

## 2. Fungsi/Endpoint Critical

- `GET /api/listings?status=ACTIVE&page=0&size=20`
- `GET /api/listings?category=ELECTRONICS&status=ACTIVE&page=0&size=20`
- `GET /api/listings/{id}`
- `GET /api/listings/{id}/validation`

## 3. Alasan Critical

Catalog list/search sering dipakai buyer dan gateway. Detail dan validation dipakai sebelum bidding, sehingga latency dan reliability berpengaruh ke user flow.

## 4. Tool

Tools yang tersedia di repo:

1. JUnit/Spring Boot method-level load-sensitive simulation via `profilingTest`.
2. Manual HTTP load script: `performance/listing-load-test.js`.

## 5. Command

Architecture simulation:

```powershell
.\gradlew "-Dprofiling.label=architecture" profilingTest --rerun-tasks
```

Manual HTTP load test, jika service sudah berjalan:

```powershell
node performance/listing-load-test.js
```

Optional:

```powershell
$env:BASE_URL="http://localhost:8082"
$env:LISTING_ID="<listing-id>"
$env:DURATION_SECONDS="30"
$env:CONCURRENCY="8"
node performance/listing-load-test.js
```

## 6. Dataset

JUnit simulation:

- 800 listings
- status campuran: ACTIVE, EXTENDED, DRAFT, WON, UNSOLD, CANCELLED
- sebagian listing memiliki bid
- H2 in-memory database
- warmup: 2
- measured runs: 8

## 7. Baseline Result

Baseline dari pre-optimization profiling:

| Function | Baseline average ms |
| --- | ---: |
| get-all-active-listings | 646.756 |
| search-filter-listings | 328.094 |
| get-listing-detail-by-id | 1.809 |
| validate-listing-can-receive-bid | 0.574 |

## 8. Optimization Applied

- Batch auction lookup via `findByListingIdIn`.
- Batch bid summary via `summarizeByAuctionIds`.
- Safe pagination/sort policy via `ListingPageRequestPolicy`.
- Entity index hints on listing, auction, and bid tables.
- Architecture boundary tests to prevent service responsibility drift.

## 9. After Result

Output file:

`build/reports/profiling/listing-query-architecture.csv`

| Function | Average ms | P95 ms | P99 ms | Throughput ops/sec | Error rate |
| --- | ---: | ---: | ---: | ---: | ---: |
| get-all-active-listings | 23.205 | 29.261 | 29.261 | 43.094 | 0.000 |
| search-filter-listings | 12.806 | 14.871 | 14.871 | 78.091 | 0.000 |
| get-listing-detail-by-id | 3.583 | 3.930 | 3.930 | 279.125 | 0.000 |
| validate-listing-can-receive-bid | 1.678 | 2.019 | 2.019 | 595.832 | 0.000 |

## 10. Improvement Percentage

| Function | Baseline avg ms | After avg ms | Improvement |
| --- | ---: | ---: | ---: |
| get-all-active-listings | 646.756 | 23.205 | 96.41% |
| search-filter-listings | 328.094 | 12.806 | 96.10% |

The >50% improvement claim applies only to catalog list/search functions.

## 11. Interpretasi Manfaat Architecture Tambahan

Query Optimization Architecture mengurangi lookup berulang per listing dan membuat query path lebih predictable. Safe sort policy juga mencegah client meminta sort field arbitrary yang berisiko memunculkan persistence error.

## 12. Limitasi Pengujian

- Simulation menggunakan service method, bukan full network HTTP.
- Database H2 in-memory, bukan PostgreSQL production.
- Baseline p95/p99 tidak tersedia karena baseline lama hanya menyimpan avg/min/max.
- Manual HTTP load script tersedia tetapi perlu service lokal dengan data.
