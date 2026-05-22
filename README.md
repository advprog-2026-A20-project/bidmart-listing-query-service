# BidMart Listing Query Service

Service listing/catalog read model untuk BidMart. Service ini melayani query listing, detail listing, kategori, validasi listing untuk bidding, serta command ringan milik listing boundary seperti create/update/cancel listing.

## Tanggung Jawab

- Membaca listing catalog dan filter status/category/keyword/price.
- Membaca listing detail, termasuk status efektif auction dan harga terkini.
- Membaca listing categories.
- Memvalidasi apakah listing masih bisa menerima bid.
- Membuat, mengubah, dan cancel listing milik seller sesuai boundary listing.
- Mengekspos health check.

Sumber kebenaran bid tetap berada di bidding command service. Service ini hanya menyimpan/memaparkan read model listing dan status terkait auction.

## Endpoint

```txt
GET /api/listings
POST /api/listings
GET /api/listings/{listingId}
PUT /api/listings/{listingId}
DELETE /api/listings/{listingId}
GET /api/listings/{listingId}/validation
GET /api/listings/categories
GET /api/listings/categories/tree
GET /actuator/health
```

## Run Lokal

Jalankan test:

```bash
./gradlew test
```

Jalankan quality gate dengan coverage:

```bash
./gradlew qualityGate
```

Jalankan profiling catalog/read path:

```bash
./gradlew "-Dprofiling.label=optimized" profilingTest --rerun-tasks
```

Build jar:

```bash
./gradlew bootJar
```

Build Docker image:

```bash
docker build -t bidmart-listing-query-service .
```

Run container:

```bash
docker run --env-file .env -p 8082:8082 bidmart-listing-query-service
```

## Environment Variable

```txt
PORT
SPRING_PROFILES_ACTIVE
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

## Service Boundary

Dokumentasi boundary ada di `docs/service-boundary.md`.

## Design dan Quality Notes

Refactor TDD/SOLID terbaru terdokumentasi di:

- `docs/tdd-report.md`
- `docs/solid-review.md`
- `docs/design-patterns.md`
- `docs/before-after-design.md`
- `docs/quality/final-quality-report.md`

Design pattern yang diterapkan:

- State Pattern untuk lifecycle listing.
- Strategy Pattern untuk filter listing.
- Factory Pattern untuk creation/update listing.
- Read model assembler dan mapper untuk response consistency.

## Status Migrasi

Fase saat ini:

```txt
gateway + auth-service + listing-query-service + auction-query-service + bidding-command-service + wallet-service
```
