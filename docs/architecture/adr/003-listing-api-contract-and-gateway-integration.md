# ADR 003 - Listing API Contract and Gateway Integration

## Context

BidMart frontend should call gateway as backend entry point. Internal services should keep stable contracts so gateway can proxy/compose responses without frontend directly coupling to service internals.

## Decision

Expose listing API under `/api/listings` and `/api/users/{id}/public-profile`. Keep public read endpoints unauthenticated and seller write endpoints protected by JWT seller role.

Frontend should access listing-query-service through `bidmart-gateway`; direct frontend-to-service calls are not part of the intended deployment architecture.

## Consequences

- Gateway remains entry point for frontend.
- Listing API contract is explicit and testable.
- Authorization is enforced in service as defense-in-depth even if gateway also checks role.
- API contract docs and regression tests help avoid accidental breakage.

## Alternatives Considered

- Expose listing-query-service directly to frontend: rejected because it leaks internal service topology.
- Trust gateway only for security: rejected because downstream service should still enforce sensitive operations.

## Status

Accepted.
