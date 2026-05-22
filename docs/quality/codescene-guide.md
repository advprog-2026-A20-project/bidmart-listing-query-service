# CodeScene Integration Guide

CodeScene is integrated in two safe layers:

1. GitHub Actions workflow using CodeScene CLI delta analysis.
2. Optional CodeScene SaaS/GitHub App setup for full hotspot/code health dashboards.

No CodeScene score is claimed until a real CodeScene run completes with a configured token/project.

## GitHub Actions Integration

Workflow:

`/.github/workflows/codescene.yml`

The workflow runs on:

- `pull_request`
- manual `workflow_dispatch`

It installs the CodeScene CLI and runs:

```bash
cs delta "$BASE_REF" HEAD --output-format json --pretty
```

The report is uploaded as a GitHub Actions artifact:

```txt
codescene-delta-analysis
```

## Required GitHub Secret

Add this repository secret:

```txt
CS_ACCESS_TOKEN
```

How:

1. Open GitHub repository.
2. Go to `Settings > Secrets and variables > Actions`.
3. Click `New repository secret`.
4. Name: `CS_ACCESS_TOKEN`.
5. Value: token from CodeScene project/admin configuration.

If the secret is missing, the workflow skips CodeScene analysis instead of failing the build.

## Manual Run

1. Open `Actions`.
2. Choose `CodeScene`.
3. Click `Run workflow`.
4. Use default `base_ref = origin/main`, or set another base ref if needed.
5. Download the `codescene-delta-analysis` artifact.

## CodeScene SaaS/GitHub App Setup

For full dashboards:

1. Connect the GitHub repository to CodeScene.
2. Select `bidmart-listing-query-service` as the analysis scope.
3. Enable pull request analysis.
4. Enable code health quality gates.
5. Review:
   - code health
   - hotspots
   - complexity trends
   - temporal coupling
   - knowledge distribution
   - delivery risk

## Expected Evidence

Attach these to the final assignment evidence:

- CodeScene workflow run URL
- `delta-analysis.json` artifact
- dashboard screenshot/export if CodeScene SaaS is connected
- hotspot list
- code health score
- before/after comparison around `ListingQueryService`

## Notes

CodeScene CLI requires an access token. Do not commit the token to the repository.
