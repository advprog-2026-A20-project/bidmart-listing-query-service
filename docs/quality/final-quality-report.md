# Final Quality Report

## 1. Executive Summary

Software Quality implementation for `bidmart-listing-query-service` has been improved through additional unit tests, functional tests, regression tests, secure coding fixes, profiling, measured optimization, JaCoCo quality gate, SonarQube config, OSSF Scorecard workflow, and documentation.

No false claim is made:

- Line coverage reached `90.35%`.
- Instruction coverage reached `87.88%`.
- Branch coverage reached `62.69%`.
- >=50% performance improvement was proven for list/search catalog functions only.
- SonarQube and OSSF Scorecard scores are not claimed because they were not executed against external services in this local run.

## 2. Branch Information

- Branch: `feature/listing-quality-testing-profiling`

## 3. Scope

Only `bidmart-listing-query-service` was modified.

## 4. Baseline Result

See `docs/quality/baseline-report.md`.

Baseline:

- Build/test: successful
- Instruction coverage: `70.37%`
- Static analysis dedicated tool: not yet available
- Main bottleneck: list/search path N+1-style auction/bid lookup

## 5. Unit Testing Result

See `docs/quality/unit-test-report.md`.

Result:

- `.\gradlew test` -> `BUILD SUCCESSFUL`
- 10 test classes

## 6. Functional Testing Result

See `docs/quality/functional-test-report.md`.

Functional API tests use Spring Boot + MockMvc.

## 7. Regression Testing Result

See `docs/quality/regression-test-report.md`.

Regression coverage protects cancelled listing visibility, effective status, bid validation, current bid display price, auth, and conflict behavior.

## 8. Secure Coding Result

See `docs/quality/secure-coding-report.md`.

Notable fixes:

- removed default hardcoded JWT fallback from application config
- added sanitized validation error handling
- expanded auth/ownership functional tests

## 9. Tools Implemented

- JaCoCo
- Gradle `qualityGate`
- SonarQube config
- OSSF Scorecard workflow
- MockMvc functional testing
- simple profiling harness via `profilingTest`

## 10. SonarQube / Static Analysis Result

SonarQube config exists in `sonar-project.properties`.

No SonarQube score is claimed because no SonarQube server scan was run.

## 11. OSSF Scorecard Result

OSSF workflow exists in `.github/workflows/scorecard.yml`.

No OSSF score is claimed because the workflow must run on GitHub.

## 12. CodeScene Result or Integration Guide

CodeScene is intentionally not implemented in this repository. No CodeScene score is claimed.

## 13. Selenium Result or Justification

Selenium is not relevant for this backend-only service. See `docs/quality/selenium-note.md`.

## 14. Profiling Critical Functions

See:

- `docs/quality/profiling-baseline.md`
- `docs/quality/profiling-optimization-report.md`

## 15. Optimization and 50% Improvement Evidence

Achieved for:

- `get-all-active-listings`: `96.41%` faster
- `search-filter-listings`: `95.86%` faster

Not achieved/claimed for:

- `get-listing-detail-by-id`
- `validate-listing-can-receive-bid`

## 16. Coverage and Quality Score

Coverage:

- Instruction: `87.88%`
- Line: `90.35%`
- Branch: `62.69%`

Quality score >=90 cannot be broadly claimed because SonarQube/Scorecard did not run. The >=90 claim only applies to JaCoCo line coverage.

## 17. Files Changed

Main areas:

- `build.gradle`
- `.github/workflows/scorecard.yml`
- `sonar-project.properties`
- `src/main/java/.../readmodel`
- `src/main/java/.../repository`
- `src/main/java/.../service/ListingQueryService.java`
- `src/main/java/.../exception/GlobalExceptionHandler.java`
- `src/main/resources/application.properties`
- `src/test/java/...`
- `docs/quality`

## 18. How to Run Tests

```powershell
.\gradlew test
.\gradlew qualityGate bootJar
```

## 19. How to Run Profiling

```powershell
.\gradlew "-Dprofiling.label=optimized" profilingTest --rerun-tasks
```

Output:

`build/reports/profiling/listing-query-optimized.csv`

## 20. Remaining Issues

- Instruction coverage is below 90%.
- Branch coverage is far below 90%.
- SonarQube and OSSF Scorecard require external execution for real scores.
- Profiling uses H2 and simple timing, not PostgreSQL/JMH/k6/JMeter.
- Detail and validation paths were measured but not optimized in this change.

## 21. Final Checklist

- [x] Branch `feature/listing-quality-testing-profiling` dibuat.
- [x] Build berhasil.
- [x] Unit test berhasil.
- [x] Functional test berhasil.
- [x] Regression test berhasil.
- [x] Coverage report tersedia.
- [x] Coverage target >= 90% tercapai untuk line coverage.
- [x] Angka aktual instruction/branch coverage dilaporkan.
- [x] Secure coding check dilakukan.
- [x] Tidak ada production secret tercommit.
- [x] SonarQube config tersedia.
- [x] OSSF Scorecard workflow tersedia.
- [x] CodeScene tidak diterapkan dan tidak diklaim.
- [x] Selenium justification ditulis.
- [x] Profiling baseline dilakukan.
- [x] Optimasi dilakukan berdasarkan bottleneck nyata.
- [x] Profiling ulang dilakukan.
- [x] Improvement minimal 50% tercapai untuk list/search catalog functions.
- [x] Semua perubahan terdokumentasi.
- [x] Cara verifikasi lokal jelas.
