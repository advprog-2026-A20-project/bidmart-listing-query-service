# BidMart Listing Query Service

Service read-only untuk query listing di BidMart. Service ini diekstrak dari gateway/monolith lama sebagai read-side public catalog.

## Tanggung Jawab

- Membaca listing catalog.
- Membaca listing detail.
- Membaca listing categories.
- Mengekspos health check.

Service ini bersifat read-only. Listing write command dan validasi transactional untuk bidding tidak menjadi tanggung jawab service ini.

## Endpoint

```txt
GET /api/listings
GET /api/listings/{listingId}
GET /api/listings/categories
GET /api/listings/categories/tree
GET /actuator/health
```

## Run Lokal

Jalankan test:

```bash
./gradlew test
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

## Status Migrasi

Fase saat ini:

```txt
gateway + auction-query-service + listing-query-service
```

Belum termasuk:

```txt
listing-command-service
bidding-command-service
wallet-service
auth-service
```
