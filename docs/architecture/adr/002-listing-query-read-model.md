# ADR 002 - Listing Query Read Model

## Context

Frontend and gateway need fast listing catalog responses containing status, current display price, auction id/status, and bid count. The source of truth for bid command remains bidding-command-service, but the listing catalog needs a read-friendly representation.

## Decision

Use a listing read model assembled by `ListingReadModelAssembler`. The read model combines:

- `Listing`
- linked `Auction`
- bid summary from `BidRepository`
- effective listing status from `ListingLifecyclePolicy`
- display price from highest bid or auction starting price

For list endpoints, use batch assembly to avoid per-listing auction/bid lookup.

## Consequences

- Catalog and detail responses stay consistent.
- Current price and bid count can be shown without placing bid logic in this service.
- Query path is faster after batch assembly.
- Event-driven projection remains a future improvement for stronger service independence.

## Alternatives Considered

- Query bidding/auction service over HTTP for every listing: rejected due to latency and reliability risk.
- Duplicate bid command logic here: rejected due to boundary violation.
- Event-driven projection: preferred long-term, but not required for current local microservice setup.

## Status

Accepted.
