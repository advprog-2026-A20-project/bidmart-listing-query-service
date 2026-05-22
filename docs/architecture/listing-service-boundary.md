# Listing Service Boundary

## 1. Service Name

`bidmart-listing-query-service`

## 2. Tanggung Jawab Utama

- Menyimpan/mengelola data listing sesuai boundary listing.
- Menyediakan query listing catalog.
- Menyediakan detail listing.
- Menyediakan category list/tree.
- Menyediakan public seller profile summary.
- Menyediakan validasi listing untuk bidding via `/api/listings/{listingId}/validation`.
- Menyediakan read model listing status/current display price berdasarkan linked auction/bid summary.

## 3. Bukan Tanggung Jawab Service Ini

- Place bid atau bidding command utama.
- Wallet hold/release/capture/payment.
- User login/register/JWT issuance.
- Finalisasi pemenang auction.
- Notification delivery.
- Shipping/order tracking.

## 4. Data yang Dimiliki/Disajikan

- `listing.id`
- `seller.id`
- `title`
- `description`
- `imageUrl`
- `category`
- `price`
- `status`
- `createdAt`
- `updatedAt`
- `cancelledAt`
- linked `auctionId`
- linked `auctionStatus`
- `startingPrice`
- `reservePrice`
- `minimumBidIncrement`
- `durationMinutes`
- `startsAt`
- `endsAt`
- `closedAt`
- `current display price`
- `totalBids`
- `hasBids`

## 5. Endpoint Utama

| Endpoint | Caller | Auth | Peran |
| --- | --- | --- | --- |
| `GET /api/listings` | Gateway/frontend via gateway | Public | Catalog/search/filter |
| `GET /api/listings/{id}` | Gateway/frontend via gateway | Public | Listing detail |
| `GET /api/listings/{id}/validation` | Gateway/bidding service | Public/internal | Bidding eligibility read |
| `POST /api/listings` | Gateway | SELLER | Create listing |
| `PUT /api/listings/{id}` | Gateway | SELLER owner | Update editable fields |
| `DELETE /api/listings/{id}` | Gateway | SELLER owner | Cancel listing if allowed |
| `GET /api/listings/categories` | Gateway/frontend via gateway | Public | Category enum |
| `GET /api/listings/categories/tree` | Gateway/frontend via gateway | Public | Category hierarchy |
| `GET /api/users/{id}/public-profile` | Gateway/frontend via gateway | Public | Seller profile summary |

## 6. Caller Utama

- `bidmart-gateway` sebagai public backend entry point.
- `bidmart-bidding-command-service` atau gateway route untuk validasi listing sebelum bid.

## 7. Coupling Risk dan Rekomendasi

- Listing read model masih membaca auction/bid snapshot. Ini acceptable sebagai read model, tetapi sebaiknya diupdate via event-driven projection ke depan.
- Jangan tambahkan wallet/auth/bidding command ke service ini.
- API contract perlu dijaga dengan regression/contract tests karena frontend/gateway mengandalkan field response.
