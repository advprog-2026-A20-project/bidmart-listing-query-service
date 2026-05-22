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

Scorecard is configured for `push` on `main`, scheduled runs, branch protection rule events, and manual `workflow_dispatch`. It is not triggered on `staging` push because `ossf/scorecard-action` supports the default branch for push events.

Workflow-level permissions are `read-all`; write permissions required for SARIF upload and OIDC publishing are scoped to the `scorecard` job. This follows Scorecard publishing restrictions that reject workflow-level write permissions.

## Local Quality Command

```powershell
.\gradlew qualityGate bootJar
```

Result: `BUILD SUCCESSFUL`

## Secret Handling

No token values are stored in workflows. SonarQube tokens are documented as environment/secret requirements only.
