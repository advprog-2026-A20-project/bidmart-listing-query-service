# CI Quality Report

## Existing CI

File:

- `.github/workflows/listing-query-ci.yml`

Runs:

- Gradle quality gate
- bootJar

## Added CI

File:

- `.github/workflows/scorecard.yml`

Runs:

- OSSF Scorecard
- SARIF upload

## Local Quality Command

```powershell
.\gradlew qualityGate bootJar
```

Result: `BUILD SUCCESSFUL`

## Secret Handling

No token values are stored in workflows. SonarQube/CodeScene tokens are documented as environment/secret requirements only.
