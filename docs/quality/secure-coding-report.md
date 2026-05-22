# Secure Coding Report

## Checks Performed

## Input Validation

- DTO validation is active via Jakarta Validation.
- `ListingRequestValidator` keeps service-level validation for null/blank request and positive price.
- Invalid controller request now returns sanitized `400` via `GlobalExceptionHandler`.

## Authorization / Ownership

- `POST /api/listings`, `PUT /api/listings/**`, and `DELETE /api/listings/**` require seller role.
- Update/cancel logic verifies listing ownership.
- Listing with existing bids cannot be modified.

## Mass Assignment

- Request DTOs do not expose sensitive fields such as `status`, `sellerId`, `currentPrice`, `winnerId`, or `bidCount`.
- `ListingFactory` explicitly maps allowed fields only.

## Error Handling

- `ResponseStatusException` returns sanitized API error.
- `MethodArgumentNotValidException` returns sanitized `400`.
- Generic exception handler returns `"Internal server error"` without stack trace.

## Secret Check

Command:

```powershell
rg -n --hidden -i "(password|secret|api[_-]?key|token|private key|BEGIN RSA|BEGIN PRIVATE|jdbc:postgresql://.*:.*@)" -g '!build/**' -g '!gradle/wrapper/gradle-wrapper.jar' -g '!.git/**'
```

Result:

- No production secret value was added.
- JWT default fallback was removed from `application.properties`; service now requires `JWT_SECRET`.
- Remaining matches are dependency names, placeholder env vars, test-only JWT secret constants, and local/test datasource password placeholders.

## Dependency Vulnerability / Supply Chain

- OSSF Scorecard workflow added.
- SonarQube project config added.
- No dependency vulnerability scan result is claimed because OWASP Dependency Check was not added/run in this branch.
