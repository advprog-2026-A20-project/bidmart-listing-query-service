# ADR 004 - Query Optimization and Safe Pagination

## Context

Listing catalog/search is a read-heavy path. Buyers repeatedly load listing lists and filters before opening detail pages or bidding. A naive list endpoint can become slow if every listing triggers separate auction/bid lookups.

## Decision

Use Query Optimization Architecture:

- bounded page size with a service-level maximum
- default sort by `createdAt desc`
- batch read model assembly for auction/bid summaries
- repository summary query for bid count/highest amount
- safe sort policy to restrict sortable fields
- entity index hints for frequently filtered/sorted fields

## Consequences

- Catalog list/search latency is significantly reduced.
- Query path becomes safer and more predictable.
- Invalid sort fields return client error instead of leaking persistence exceptions.
- Production DB still needs explicit migration/index management beyond JPA metadata.

## Alternatives Considered

- Cache active listing results: rejected for now because auction price/status can change often.
- Query all data and filter entirely in memory: rejected due to scalability.
- External search engine: too heavy for current project scope.

## Status

Accepted.
