# Quality Tools Report

## JaCoCo

Status: implemented.

Commands:

```powershell
.\gradlew test
.\gradlew qualityGate
```

Current coverage:

- Instruction: `87.88%`
- Line: `90.35%`
- Branch: `62.69%`

## Quality Gate

Status: implemented in Gradle.

Thresholds:

- Line coverage >= `90%`
- Instruction coverage >= `85%`

## SonarQube

Status: configuration added.

File:

- `sonar-project.properties`

Run example:

```powershell
.\gradlew test jacocoTestReport
sonar-scanner -Dsonar.host.url=$env:SONAR_HOST_URL -Dsonar.token=$env:SONAR_TOKEN
```

No SonarQube score is claimed because the scanner was not run against a SonarQube server in this local task.

## OSSF Scorecard

Status: GitHub Actions workflow added.

File:

- `.github/workflows/scorecard.yml`

No OSSF Scorecard score is claimed because the workflow must run on GitHub.

## CodeScene

Status: guide added.

File:

- `docs/quality/codescene-guide.md`

No CodeScene score is claimed because CodeScene requires project integration/access.

## Selenium

Status: not used in this backend service.

Justification:

- This repo has no browser UI.
- Selenium belongs in frontend/E2E repository.
