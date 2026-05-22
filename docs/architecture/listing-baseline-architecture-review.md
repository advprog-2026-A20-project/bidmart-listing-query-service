# Listing Query Service Baseline Architecture Review

## 1. Ringkasan Struktur Service

`bidmart-listing-query-service` adalah Spring Boot microservice yang menjadi boundary listing/catalog BidMart. Service ini menerima request HTTP dari gateway atau service internal lain, membaca/menulis data listing sesuai scope listing, dan menyediakan read model listing yang menggabungkan data listing, auction status, dan bid summary.

## 2. Layer yang Ditemukan

| Layer | Package/File | Peran |
| --- | --- | --- |
| Controller | `controller/ListingQueryController`, `controller/PublicUserController` | HTTP endpoint dan request/response boundary |
| Application service | `service/ListingQueryService` | Orchestration use case listing |
| Domain model/entity | `model/Listing`, `Auction`, `Bid`, `User`, enum status/category/role | Data/domain state listing read model |
| Repository | `repository/*Repository` | Data access JPA |
| DTO | `dto/*Request`, `dto/*Response` | API contract payload |
| Mapper/read model | `readmodel/ListingReadModelAssembler`, `ListingResponseMapper` | Response assembly, current price, total bid, effective status |
| Validator/policy | `validation/ListingRequestValidator`, `lifecycle/ListingLifecyclePolicy` | Input validation and status rules |
| Filter strategies | `filter/*SpecificationStrategy` | Query specification composition |
| Security/config | `security/*`, `config/*` | JWT validation, HTTP security, Jackson/request logging |

## 3. Endpoint yang Ditemukan

- `GET /api/listings`
- `POST /api/listings`
- `GET /api/listings/{listingId}`
- `PUT /api/listings/{listingId}`
- `DELETE /api/listings/{listingId}`
- `GET /api/listings/{listingId}/validation`
- `GET /api/listings/categories`
- `GET /api/listings/categories/tree`
- `GET /api/users/{userId}/public-profile`
- `GET /actuator/health`

## 4. Service Boundary

Service ini sesuai boundary listing/catalog:

- menyediakan query listing
- menyediakan detail listing
- membuat/mengubah/cancel listing sesuai ownership dan status
- menyediakan validasi listing untuk bidding service/gateway
- menyajikan current display price dari highest bid/read model

Service ini tidak mengambil tanggung jawab service lain:

- tidak melakukan place bid sebagai command utama
- tidak mengelola wallet hold/release/capture
- tidak menerbitkan JWT/login/register
- tidak menentukan winner auction secara penuh
- tidak mengirim notification

## 5. Dependency

Runtime dependency utama:

- PostgreSQL/H2 melalui Spring Data JPA
- JWT secret untuk memvalidasi token dari auth service/gateway
- caller HTTP: gateway dan internal service yang membutuhkan validasi listing

Tidak ditemukan HTTP client outbound ke service lain, sehingga belum perlu timeout/fallback external call.

## 6. Data Ownership

Data utama yang dibaca/dimiliki dalam konteks read model:

- listing id, title, description, image URL, category, seller id, status, price, timestamp
- auction id/status/start/end/price/increment sebagai linked read model
- bid amount summary untuk display price dan total bids
- user id/email/role untuk seller public profile

Catatan: pada microservice ideal jangka panjang, tabel auction/bid/user sebaiknya diperlakukan sebagai replicated read model atau event-projected snapshot, bukan shared-write ownership lintas service.

## 7. Architecture Risk

- Beberapa data auction/bid masih berada di database yang sama sebagai read model. Ini praktis untuk local microservices, tetapi coupling data perlu dijaga.
- Query status efektif membutuhkan auction state, sehingga sebagian filter masih dilakukan setelah read model assembly.
- Production schema migration untuk index belum terlihat; entity index hanya membantu saat Hibernate membuat schema local/test.
- `application.properties` masih punya default datasource local; production profile harus selalu mengisi env var real via deployment secret.

## 8. Build/Test/Run Status Awal

Command:

```powershell
.\gradlew qualityGate bootJar
```

Result: `BUILD SUCCESSFUL`

## 9. Rekomendasi Improvement

- Tambahkan explicit API contract documentation.
- Tambahkan ADR untuk service boundary, read model, dan gateway integration.
- Perkuat query optimization architecture dengan bounded pagination, safe sorting, batch read model assembly, dan index hints.
- Tambahkan architecture boundary tests agar service tidak bergeser menjadi bidding/wallet/auth service.
- Tambahkan load/security testing simulation untuk membuktikan manfaat architecture tambahan.
