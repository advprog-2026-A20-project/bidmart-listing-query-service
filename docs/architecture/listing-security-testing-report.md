# Listing Security Testing Report

## 1. Tujuan

Memastikan architecture listing-query-service menjaga boundary security dasar: auth untuk write endpoint, ownership guard, mass-assignment prevention, sanitized error, dan secret hygiene.

## 2. Tools

- Spring Boot + MockMvc security/functional tests.
- Manual PowerShell security smoke script: `security/listing-security-check.ps1`.
- Secret scan with `rg`.

## 3. Checks

| Check | Evidence | Status |
| --- | --- | --- |
| Write endpoint tanpa auth ditolak | `ListingQueryIntegrationTest.createListingShouldRequireSellerAuthentication` | OK |
| Non-owner / ownership guard | service `getOwnedEditableListing`, auth tests | OK |
| Listing dengan bid tidak bisa diedit | `updateListingWithExistingBidShouldBeRejected` | OK |
| Mass assignment status/seller/currentPrice diabaikan | `createListingShouldIgnoreMassAssignedStatusAndSellerIdFields` | OK |
| Invalid payload return 400 sanitized | `invalidCreateRequestShouldReturnBadRequestWithoutStackTrace` | OK |
| Tidak ada endpoint bidding/wallet/auth command | `ListingServiceArchitectureTest` | OK |
| Secret scan manual tersedia | `security/listing-security-check.ps1` | OK |

## 4. Command

Automated:

```powershell
.\gradlew test
```

Manual smoke against running service:

```powershell
.\security\listing-security-check.ps1 -BaseUrl http://localhost:8082
```

## 5. Secret Scan Pattern

The script checks common risky patterns:

- private key material
- API key assignment
- password embedded in PostgreSQL URL

## 6. Result

Latest automated result:

```txt
.\gradlew clean test -> BUILD SUCCESSFUL
```

No production credential was added by this architecture work.

## 7. Limitations

- OWASP ZAP was not run.
- Dependency vulnerability scan was not added in this task.
- Full CORS production hardening depends on deployment/gateway configuration.
