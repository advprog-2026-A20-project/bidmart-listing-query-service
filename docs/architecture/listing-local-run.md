# Listing Query Service Local Run

## Build

```powershell
.\gradlew bootJar
```

## Test

```powershell
.\gradlew test
.\gradlew qualityGate bootJar
```

## Run Local

```powershell
.\gradlew bootRunLocal
```

Default local port:

```txt
8082
```

## Environment Variables

```txt
PORT=8082
SPRING_PROFILES_ACTIVE=local
SPRING_DATASOURCE_URL=jdbc:h2:mem:listingquery;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=
JWT_SECRET=<at-least-32-character-secret>
```

For Docker/PostgreSQL, use `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` from environment variables or secret manager.

## Smoke Test

Health:

```powershell
curl http://localhost:8082/actuator/health
```

Expected:

```json
{"status":"UP"}
```

List:

```powershell
curl "http://localhost:8082/api/listings?page=0&size=20"
```

Categories:

```powershell
curl http://localhost:8082/api/listings/categories
```

Validation:

```powershell
curl http://localhost:8082/api/listings/<listing-id>/validation
```

## Notes

Write endpoints require a valid JWT with seller role. In local end-to-end setup, call them through `bidmart-gateway`.
