# Listing API Contract

Base path: `/api`

Error response:

```json
{
  "timestamp": "2026-05-22T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Title is required",
  "path": "/api/listings"
}
```

## `GET /api/listings`

Auth: public.

Query params:

- `page`
- `size`
- `sort`
- `category`
- `keyword`
- `minPrice`
- `maxPrice`
- `status`
- `endingAfter`
- `endingBefore`

Response `200`:

```json
[
  {
    "id": "uuid",
    "title": "Gaming Phone",
    "description": "Competitive smartphone",
    "imageUrl": "https://img.example/phone.jpg",
    "price": 1500.00,
    "category": "ELECTRONICS",
    "categoryLabel": "Elektronik",
    "sellerId": "uuid",
    "sellerEmail": "seller@example.com",
    "status": "ACTIVE",
    "auctionId": "uuid",
    "auctionStatus": "ACTIVE",
    "auctionEndsAt": "2026-05-22T12:00:00Z",
    "totalBids": 2,
    "hasBids": true,
    "createdAt": "2026-05-22T10:00:00Z",
    "updatedAt": null,
    "cancelledAt": null
  }
]
```

## `POST /api/listings`

Auth: `SELLER`.

Request:

```json
{
  "title": "Desk Setup",
  "description": "Standing desk",
  "imageUrl": "https://img.example/desk.jpg",
  "price": 750.00,
  "category": "HOME_LIVING_FURNITURE"
}
```

Response:

- `201 Created` with `ListingResponse`
- `400` invalid payload
- `401/403` missing/invalid role

## `GET /api/listings/{listingId}`

Auth: public.

Response:

- `200` with `ListingDetailResponse`
- `404` when listing does not exist

Detail response includes listing fields plus auction pricing/timing fields:

- `startingPrice`
- `reservePrice`
- `minimumBidIncrement`
- `durationMinutes`
- `auctionStartsAt`
- `auctionEndsAt`
- `auctionClosedAt`

## `PUT /api/listings/{listingId}`

Auth: `SELLER` owner.

Request:

```json
{
  "description": "Updated description",
  "imageUrl": "https://img.example/new.jpg",
  "category": "ELECTRONICS"
}
```

Response:

- `200` with `ListingDetailResponse`
- `403` non-owner
- `404` missing listing
- `409` listing/auction not editable or already has bids

## `DELETE /api/listings/{listingId}`

Auth: `SELLER` owner.

Response:

- `200` with cancelled `ListingDetailResponse`
- `403` non-owner
- `404` missing listing
- `409` not cancellable

## `GET /api/listings/{listingId}/validation`

Auth: public/internal.

Response:

```json
{
  "listingId": "uuid",
  "active": true,
  "biddable": true,
  "message": "Listing is valid for bidding",
  "listingStatus": "ACTIVE",
  "auctionStatus": "ACTIVE",
  "endsAt": "2026-05-22T12:00:00Z"
}
```

## Category Endpoints

- `GET /api/listings/categories`
- `GET /api/listings/categories/tree`

Auth: public.

## `GET /api/users/{userId}/public-profile`

Auth: public.

Response:

```json
{
  "id": "uuid",
  "email": "seller@example.com",
  "role": "SELLER",
  "activeListingCount": 2,
  "liveAuctionCount": 1,
  "completedAuctionCount": 1
}
```
