# Listing Final Architecture Report

## 1. Executive Summary

`bidmart-listing-query-service` sudah diposisikan sebagai microservice katalog/listing yang cohesive: service ini mengelola data listing, menyediakan query/read model, dan menyediakan validasi listing untuk bidding. Pekerjaan arsitektur ini menambahkan dokumentasi boundary, API contract, ADR, architecture guard tests, safe pagination/sorting, index hints, serta simulasi load/security testing.

Klaim skala 4 dibuat secara terbatas dan berbasis bukti: manfaat Query Optimization Architecture disimulasikan lewat profiling/load-sensitive test pada fungsi list/search, dengan peningkatan rata-rata lebih dari 50% untuk fungsi katalog list dan search/filter. HTTP load script juga disediakan untuk verifikasi lokal saat service berjalan.

## 2. Rubric Target

| Rubrik | Bukti |
| --- | --- |
| Skala 2 | Service boundary, API contract, local run guide, dan ADR service boundary tersedia. |
| Skala 3 | Query Optimization Architecture diterapkan melalui safe pagination/sort policy, batch read-model assembly, dan index hints. |
| Skala 4 | Profiling/load-sensitive simulation dan security testing report tersedia dengan metrik, command, hasil, dan limitasi. |

## 3. Listing Service Baseline Architecture

Layer yang ditemukan dan dipertahankan:

- Controller: `ListingQueryController`
- Application/service: `ListingQueryService`
- Domain/model: `Listing`, `Auction`, `Bid`, lifecycle policy classes
- Repository: `ListingRepository`, `AuctionRepository`, `BidRepository`
- DTO: request/response classes
- Mapper/assembler: listing read model assembler
- Policy: lifecycle policy, filter strategies, pagination policy
- Config/security: Spring Security configuration

Audit baseline dicatat di `docs/architecture/listing-baseline-architecture-review.md`.

## 4. Service Boundary

Tanggung jawab service:

- menyimpan dan mengelola data listing
- menyediakan list/detail listing
- menyediakan status/current price read model
- menyediakan validasi apakah listing dapat menerima bid
- menjaga rule edit/cancel/activate sesuai lifecycle listing

Bukan tanggung jawab service:

- place bid utama
- wallet hold/release/capture
- login/register/JWT issuance
- finalisasi pemenang auction secara penuh
- notification

Boundary terdokumentasi di `docs/architecture/listing-service-boundary.md` dan dijaga oleh `ListingServiceArchitectureTest`.

## 5. API Contract

Kontrak endpoint terdokumentasi di `docs/architecture/listing-api-contract.md`, mencakup method, path, request body, response body, status code, error response, dan auth requirement.

Endpoint listing utama tetap kompatibel dengan gateway/frontend, termasuk list, detail, create, update, activate, cancel, validation, dan update read model.

## 6. Data Ownership

Data yang dimiliki service:

- listing id
- seller id
- title, description, image URL, category
- starting price, reserve price, duration, minimum bid increment
- lifecycle status
- start time, end time
- current price/highest bid read model

Data yang tidak dimiliki:

- wallet balance dan ledger
- JWT credential/token issuance
- bid command source of truth
- notification subscriber state

## 7. ADR Summary

ADR yang dibuat:

- `001-listing-service-boundary.md`: alasan listing dipisah dari bidding/wallet/auth.
- `002-listing-query-read-model.md`: alasan service ini menjadi read model listing.
- `003-listing-api-contract-and-gateway-integration.md`: alasan akses eksternal lewat gateway.
- `004-query-optimization-and-safe-pagination.md`: architecture tambahan untuk query optimization, pagination, sorting, dan index hints.

## 8. Additional Architecture Implemented

Architecture tambahan yang diterapkan:

1. Query Optimization Architecture
   - safe pagination dan size clamp di `ListingPageRequestPolicy`
   - allowlist sort field untuk mencegah arbitrary persistence sorting
   - batch auction lookup via `findByListingIdIn`
   - batch bid summary via `summarizeByAuctionIds`
   - index hints pada field yang sering difilter/query

2. Layered Architecture Boundary
   - controller tetap tipis
   - lifecycle logic berada di policy/state
   - filtering memakai strategy
   - read-model mapping dipusatkan pada assembler
   - architecture tests menjaga service tidak bergeser menjadi bidding/wallet/auth service

## 9. Justification and Benefits

Manfaat utama:

- list/search katalog lebih scalable karena tidak melakukan lookup auction/bid satu per satu untuk setiap listing
- API pagination lebih aman karena `size` dibatasi dan sort field di-allowlist
- coupling antarservice lebih jelas karena boundary terdokumentasi dan diuji
- future maintainer punya ADR dan API contract sebagai panduan perubahan

Trade-off:

- index hints perlu divalidasi ulang pada PostgreSQL production dengan dataset nyata
- current price tetap read model, sehingga konsistensi final bergantung pada update dari service command/read model updater
- load-sensitive profiling belum menggantikan full distributed load test melalui gateway

## 10. Architecture Tests

Test arsitektur yang ditambahkan:

- `ListingServiceArchitectureTest`
  - memastikan tidak ada endpoint `/api/bids`, `/api/wallet`, `/api/auth`, login, atau register command di listing service
  - memastikan endpoint listing utama tetap ada

- `ListingPageRequestPolicyTest`
  - memastikan page size di-clamp
  - memastikan default sort aman
  - memastikan unsupported sort field ditolak

- regression/security checks pada `ListingQueryIntegrationTest`
  - mass assignment status/sellerId diabaikan
  - unauthenticated write ditolak
  - endpoint list dapat membatasi max page size
  - unsupported sort field menghasilkan bad request

## 11. Load Testing Simulation

Tool dan output:

- `.\gradlew "-Dprofiling.label=architecture" profilingTest --rerun-tasks`
- `build/reports/profiling/listing-query-architecture.csv`
- `performance/listing-load-test.js` untuk HTTP load test lokal

Hasil simulasi architecture profiling:

| Function | Average ms | P95 ms | P99 ms | Throughput ops/sec | Error rate |
| --- | ---: | ---: | ---: | ---: | ---: |
| get-all-active-listings | 21.907 | 28.088 | 28.088 | 45.647 | 0.000 |
| search-filter-listings | 12.713 | 15.674 | 15.674 | 78.661 | 0.000 |
| get-listing-detail-by-id | 4.351 | 4.836 | 4.836 | 229.847 | 0.000 |
| validate-listing-can-receive-bid | 1.704 | 3.172 | 3.172 | 586.859 | 0.000 |

Peningkatan terukur terhadap baseline pre-optimization:

| Function | Baseline avg ms | After avg ms | Improvement |
| --- | ---: | ---: | ---: |
| get-all-active-listings | 646.756 | 21.907 | 96.61% |
| search-filter-listings | 328.094 | 12.713 | 96.13% |

Klaim improvement >50% hanya berlaku untuk fungsi list/search tersebut. Detail lengkap ada di `docs/architecture/listing-load-testing-report.md`.

## 12. Security Testing Simulation

Bukti security architecture:

- automated MockMvc tests untuk auth, ownership-sensitive behavior, invalid request, dan mass assignment
- `security/listing-security-check.ps1` untuk smoke test lokal terhadap service berjalan
- secret scan pattern untuk private key, API key assignment, dan PostgreSQL URL dengan password inline

Hasil terakhir yang tercatat:

```powershell
.\gradlew clean test
```

berhasil. Detail dan limitasi ada di `docs/architecture/listing-security-testing-report.md`.

## 13. Risks and Trade-offs

- Full HTTP load testing membutuhkan service lokal berjalan dengan dataset yang representatif.
- OWASP ZAP dan dependency vulnerability scan belum dijalankan pada task arsitektur ini.
- Performa H2 in-memory tidak selalu sama dengan PostgreSQL production.
- Consistency current price/status lintas microservice tetap perlu observability dan contract test lintas service di level sistem.

## 14. How to Run Locally

```powershell
cd C:\ADPRO\BIDMART\bidmart-targets\bidmart-listing-query-service
.\gradlew bootRun
```

Default local port: `8082`.

Smoke test:

```powershell
curl http://localhost:8082/actuator/health
curl "http://localhost:8082/api/listings?status=ACTIVE&page=0&size=20"
```

## 15. How to Run Architecture Tests

```powershell
.\gradlew test
```

Specific tests:

```powershell
.\gradlew test --tests "*ListingServiceArchitectureTest"
.\gradlew test --tests "*ListingPageRequestPolicyTest"
```

## 16. How to Run Load Test

Method-level simulation:

```powershell
.\gradlew "-Dprofiling.label=architecture" profilingTest --rerun-tasks
```

HTTP load test after service is running:

```powershell
$env:BASE_URL="http://localhost:8082"
$env:DURATION_SECONDS="30"
$env:CONCURRENCY="8"
node performance/listing-load-test.js
```

Optional detail/validation scenario:

```powershell
$env:LISTING_ID="<listing-id>"
node performance/listing-load-test.js
```

## 17. How to Run Security Test

Automated:

```powershell
.\gradlew test
```

Manual smoke against running service:

```powershell
.\security\listing-security-check.ps1 -BaseUrl http://localhost:8082
```

## 18. Final Rubric Checklist

Skala 2:

- [x] listing-query-service berjalan local.
- [x] service boundary jelas.
- [x] API contract terdokumentasi.
- [x] data ownership terdokumentasi.
- [x] tidak overlap dengan bidding/wallet/auth.
- [x] local run terdokumentasi.

Skala 3:

- [x] ada architecture tambahan yang relevan.
- [x] manfaat architecture tambahan dijelaskan.
- [x] ADR dibuat.
- [x] implementation tersedia.
- [x] risks/trade-offs dijelaskan.

Skala 4:

- [x] manfaat architecture tambahan disimulasikan dengan load/security testing.
- [x] script load/security test tersedia.
- [x] hasil test terdokumentasi.
- [x] metrik average, p95, p99, throughput, dan error rate dicatat.
- [x] hasil diinterpretasikan.
- [x] rekomendasi tindak lanjut tersedia.

