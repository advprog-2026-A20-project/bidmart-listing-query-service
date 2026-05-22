# Design Patterns - Listing Query Service

## 1. State Pattern

- Problem: rule status listing/auction seperti DRAFT, ACTIVE, EXTENDED, WON, UNSOLD, dan CANCELLED sebelumnya ada sebagai helper conditional di service.
- Files: `lifecycle/ListingState.java`, `DraftListingState.java`, `ActiveListingState.java`, `ExtendedListingState.java`, `TerminalListingState.java`, `ListingLifecyclePolicy.java`.
- Alasan sesuai: lifecycle BidMart punya behavior berbeda per status, misalnya `canEdit` dan `canReceiveBid`.
- Before: service punya helper status dan switch langsung.
- After: service bertanya ke policy/state.
- Bukan overengineering: status lifecycle adalah core domain rule dan dipakai di update, cancel, filter, dan validation.

## 2. Strategy Pattern

- Problem: filter listing category, keyword, dan price range sebelumnya menumpuk sebagai private method di service.
- Files: `filter/ListingSpecificationStrategy.java`, `ListingSpecificationBuilder.java`, `CategoryListingSpecificationStrategy.java`, `KeywordListingSpecificationStrategy.java`, `PriceRangeListingSpecificationStrategy.java`.
- Alasan sesuai: setiap filter adalah variasi query rule yang bisa bertambah.
- Before: penambahan filter baru perlu mengubah `ListingQueryService`.
- After: penambahan filter baru cukup menambah strategy baru.
- Bukan overengineering: endpoint list/search adalah fungsi utama listing-query-service.

## 3. Factory Pattern

- Problem: pembuatan listing dan update editable fields tersebar dan rawan default field tidak konsisten.
- Files: `factory/ListingFactory.java`.
- Alasan sesuai: creation logic punya default category, status ACTIVE, timestamp, dan normalization.
- Before: service membangun entity secara langsung.
- After: service mendelegasikan object creation/update ke factory.
- Bukan overengineering: factory kecil dan hanya membungkus aturan creation yang sudah ada.

## 4. Mapper / Read Model Assembler

- Problem: response summary/detail butuh status efektif, auction, total bid, dan display price, sehingga service menjadi panjang.
- Files: `readmodel/ListingReadModel.java`, `ListingReadModelAssembler.java`, `ListingResponseMapper.java`.
- Alasan sesuai: listing-query-service adalah read side; assembling read model adalah boundary terpisah dari orchestration.
- Before: mapping DTO dan lookup bid/auction ada di service.
- After: assembler dan mapper menangani read model.
- Bukan overengineering: logic harga tampil harus konsisten di summary dan detail.
