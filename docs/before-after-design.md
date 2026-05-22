# Before vs After Design

## Before

- `ListingQueryService` mengandung orchestration, validation, factory logic, status lifecycle, JPA specification filter, response mapping, dan display price calculation.
- Filter query sulit diperluas karena semua rule berada di service.
- Status lifecycle sulit diaudit karena rule edit/cancel/bid tersebar.
- Mapping summary/detail rawan tidak konsisten, khususnya harga terkini dari auction bid.

## After

- `ListingQueryService` lebih fokus sebagai application service.
- Lifecycle rule dipusatkan di `ListingLifecyclePolicy` dan `ListingState`.
- Validation pindah ke `ListingRequestValidator`.
- Creation/update entity pindah ke `ListingFactory`.
- Query filter pindah ke strategy classes.
- Read model dan response mapping pindah ke `ListingReadModelAssembler` dan `ListingResponseMapper`.

## Maintainability Benefit

- Menambah status rule baru tidak perlu menyentuh banyak helper service.
- Menambah filter baru cukup menambah strategy.
- Response summary/detail memakai jalur assembly yang sama sehingga harga terkini konsisten.
- Unit test bisa menarget class kecil tanpa harus selalu memakai Spring context.

## Evidence

- Baseline sebelum refactor: `.\gradlew test bootJar` -> `BUILD SUCCESSFUL`
- Setelah refactor: `.\gradlew qualityGate bootJar` -> `BUILD SUCCESSFUL`
- JaCoCo instruction coverage: `70.37%`

## Remaining Risk

- Query listing masih melakukan sebagian filter status/window di memory karena status efektif bergantung pada auction linked state.
- Tidak ada perubahan kontrak API; regression behavior dijaga lewat integration test.
