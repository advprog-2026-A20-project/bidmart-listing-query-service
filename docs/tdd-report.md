# TDD Report - Listing Query Service

## Baseline

- Branch awal: `fix/listing-cancelled-price-readmodel`
- Branch kerja: `quality/listing-tdd-solid-patterns`
- Baseline command: `.\gradlew test bootJar`
- Baseline result: `BUILD SUCCESSFUL`

## Characterization Tests

Tests yang dipakai sebagai safety net:

- `ListingQueryIntegrationTest.cancelledListingDetailShouldRemainVisible`
- `ListingQueryIntegrationTest.listingSummaryAndDetailPriceShouldFollowHighestAuctionBid`
- `ListingQueryIntegrationTest.listingPriceShouldUseAuctionStartingPriceBeforeAnyBid`
- `ListingQueryIntegrationTest.statusFilterAndBidValidationShouldUseEffectiveAuctionStatus`
- `ListingReadModelAssemblerTest.assembleShouldUseHighestBidAsDisplayPriceWhenAuctionHasBids`
- `ListingReadModelAssemblerTest.assembleShouldUseAuctionStartingPriceBeforeAnyBid`

## TDD Cycle 1 - Lifecycle, Validation, Factory

Feature: extract lifecycle policy, request validation, and listing creation/update factory.

RED:
- Test name: `ListingLifecyclePolicyTest`, `ListingRequestValidatorTest`, `ListingFactoryTest`
- Kenapa gagal: class target belum ada sehingga `compileTestJava` gagal.

GREEN:
- Perubahan minimal: menambahkan `ListingLifecyclePolicy`, state classes, `ListingRequestValidator`, dan `ListingFactory`.
- Test result: `.\gradlew test` -> `BUILD SUCCESSFUL`

REFACTOR:
- Refactor yang dilakukan: `ListingQueryService` memakai lifecycle policy, validator, dan factory.
- Design benefit: validasi, lifecycle rule, dan object creation tidak lagi bercampur di service.
- Test result: `.\gradlew test` -> `BUILD SUCCESSFUL`

## TDD Cycle 2 - Filter Strategy

Feature: extract listing filter strategies.

RED:
- Test name: `ListingSpecificationBuilderTest`
- Kenapa gagal: builder/strategy filter belum tersedia.

GREEN:
- Perubahan minimal: menambahkan `ListingSpecificationStrategy`, `ListingSpecificationBuilder`, dan strategi distinct/category/keyword/price range.
- Test result: `.\gradlew test` -> `BUILD SUCCESSFUL`

REFACTOR:
- Refactor yang dilakukan: `ListingQueryService.getAllListings` memakai `ListingSpecificationBuilder`.
- Design benefit: filter baru bisa ditambahkan lewat strategy baru tanpa menambah conditional di service utama.
- Test result: `.\gradlew test` -> `BUILD SUCCESSFUL`

## TDD Cycle 3 - Read Model Assembly

Feature: extract listing read model assembly and response mapping.

RED:
- Test name: `ListingReadModelAssemblerTest`
- Kenapa gagal: assembler/read model belum tersedia.

GREEN:
- Perubahan minimal: menambahkan `ListingReadModel`, `ListingReadModelAssembler`, dan `ListingResponseMapper`.
- Test result: `.\gradlew test` -> `BUILD SUCCESSFUL`

REFACTOR:
- Refactor yang dilakukan: summary/detail response dibuat melalui assembler, termasuk status efektif, total bid, dan display price.
- Design benefit: mapping response dan perhitungan read model tidak lagi mendominasi service.
- Test result: `.\gradlew test` -> `BUILD SUCCESSFUL`

## Final Validation

- `.\gradlew qualityGate bootJar` -> `BUILD SUCCESSFUL`
- JaCoCo instruction coverage: `70.37%`
