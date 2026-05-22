# Functional Test Report

## Tool

- Spring Boot Test
- MockMvc
- H2 in-memory database

## Covered Flows

- `GET /actuator/health`
- `GET /api/listings`
- `GET /api/listings/{listingId}`
- `GET /api/listings/categories`
- `GET /api/listings/categories/tree`
- `GET /api/listings/{listingId}/validation`
- `GET /api/users/{userId}/public-profile`
- `POST /api/listings`
- `PUT /api/listings/{listingId}`

## Key Functional Assertions

- Cancelled listing detail remains visible.
- Listing summary/detail price follows highest auction bid.
- Listing price falls back to auction starting price before any bid.
- Status filter uses effective auction status.
- WON listing is not biddable.
- Create listing requires seller authentication.
- Invalid create request returns `400` without stack trace.
- Update listing with existing bid returns `409`.
- Public seller profile returns listing and auction counts.

## Command

```powershell
.\gradlew test
```

## Result

Result: `BUILD SUCCESSFUL`

## Selenium Note

Selenium is not used in this backend-only service. UI/E2E Selenium belongs in `bidmart-frontend`, where browser interactions exist. See `docs/quality/selenium-note.md`.
