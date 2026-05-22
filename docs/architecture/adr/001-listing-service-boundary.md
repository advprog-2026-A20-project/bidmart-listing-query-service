# ADR 001 - Listing Service Boundary

## Context

BidMart is split into microservices for auth, gateway, listing query, auction query, bidding command, wallet, and infra. Listing-related read and seller listing operations need a clear boundary to avoid monolith-style coupling.

## Decision

Keep `bidmart-listing-query-service` responsible for listing/catalog data, listing detail, seller listing create/update/cancel within listing boundary, category reads, public seller profile summary, and listing validation for bid eligibility.

Do not place bidding command, wallet, auth token issuance, winner finalization, or notification logic in this service.

## Consequences

- High cohesion around listing/catalog behavior.
- Gateway and bidding service can call a stable listing validation contract.
- Other services remain owners of bid command, wallet balance, auth, and auction outcome.
- Some auction/bid data exists as read model dependency and should be managed carefully.

## Alternatives Considered

- Put all listing/bidding/auction logic in one service: rejected because it recreates monolith boundary.
- Make listing-query-service read-only only: rejected for current project state because seller listing commands already exist in this bounded service.

## Status

Accepted.
