# Regression Test Report

## Regression-Sensitive Behavior

Regression tests protect behavior that previously changed or is easy to break:

- CANCELLED listing detail remains visible.
- Listing price follows current highest bid.
- Listing price uses auction starting price before any bid.
- Listing and auction status are aligned via effective status.
- WON listing rejects bid validation.
- Existing bid blocks listing update/cancel.
- Response DTO fields used by frontend/gateway remain present.
- HTTP status codes for invalid/unauthorized/conflict cases remain stable.

## Test Locations

- `ListingQueryIntegrationTest`
- `ListingReadModelAssemblerTest`
- `ListingLifecyclePolicyTest`

## Command

```powershell
.\gradlew test
```

## Result

Result: `BUILD SUCCESSFUL`

## Notes

The service does not currently expose activate listing or draft auction creation semantics. Those test items are marked not applicable for this repo rather than being faked.
