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
- `.github/workflows/codescene.yml`

Runs:

- OSSF Scorecard
- SARIF upload
- CodeScene CLI delta analysis when `CS_ACCESS_TOKEN` is configured

Scorecard is configured for `push` on `main`, scheduled runs, branch protection rule events, and manual `workflow_dispatch`. It is not triggered on `staging` push because `ossf/scorecard-action` supports the default branch for push events.

Workflow-level permissions are `read-all`; write permissions required for SARIF upload and OIDC publishing are scoped to the `scorecard` job. This follows Scorecard publishing restrictions that reject workflow-level write permissions.

CodeScene is configured for pull requests and manual runs. It skips safely if `CS_ACCESS_TOKEN` is missing.

## Local Quality Command

```powershell
.\gradlew qualityGate bootJar
```

Result: `BUILD SUCCESSFUL`

## Secret Handling

No token values are stored in workflows. SonarQube/CodeScene tokens are documented as environment/secret requirements only.
