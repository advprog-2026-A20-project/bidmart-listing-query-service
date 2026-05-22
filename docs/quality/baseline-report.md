# Baseline Quality Report

## 1. Build Status Awal

Command:

```powershell
.\gradlew qualityGate bootJar
```

Result: `BUILD SUCCESSFUL`

## 2. Test Status Awal

Command `.\gradlew qualityGate bootJar` menjalankan unit/functional test existing dan JaCoCo verification.

Result: `BUILD SUCCESSFUL`

## 3. Coverage Awal

- Tool: JaCoCo
- Instruction coverage: `70.37%`
- Coverage threshold existing: `45%`
- Status target 90%: belum tercapai.

## 4. Static Analysis Awal

- Tool existing: JaCoCo coverage gate.
- Static analysis dedicated seperti Checkstyle/PMD/SpotBugs belum tersedia pada baseline.
- SonarQube/CodeScene/OSSF Scorecard belum tersedia pada baseline.

## 5. Endpoint/Fungsi Critical

1. `get all active listings`
   - Critical karena dipakai buyer untuk catalog/homepage.
2. `search/filter listings`
   - Critical karena query category/keyword/price/status bisa menyentuh banyak data.
3. `get listing detail by id`
   - Critical karena dipakai sebelum buyer melihat auction/bid detail.
4. `validate listing can receive bid`
   - Critical karena bidding-command-service dapat memakai validasi ini sebelum menerima bid.

Endpoint activate listing dan update current price/read model tidak tersedia sebagai endpoint eksplisit di listing-query-service saat baseline ini diambil.

## 6. Profiling Baseline

Command:

```powershell
.\gradlew "-Dprofiling.label=baseline" profilingTest
```

Dataset:

- `800` listings
- status campuran: ACTIVE, EXTENDED, DRAFT, WON, UNSOLD, CANCELLED
- sebagian listing memiliki bid
- H2 in-memory database
- warmup runs: `2`
- measured runs: `8`

Output CSV:

`build/reports/profiling/listing-query-baseline.csv`

| Function | Average ms | Min ms | Max ms |
| --- | ---: | ---: | ---: |
| get-all-active-listings | 646.756 | 545.011 | 773.436 |
| search-filter-listings | 328.094 | 281.033 | 384.480 |
| get-listing-detail-by-id | 1.809 | 1.575 | 2.132 |
| validate-listing-can-receive-bid | 0.574 | 0.501 | 0.649 |

## 7. Baseline Bottleneck Hypothesis

List/search endpoint melakukan lookup auction/status dan bid summary per listing. Ini menunjukkan potensi N+1 query pada catalog path, sehingga optimasi pertama akan diarahkan ke batch read model assembly untuk list endpoint.
