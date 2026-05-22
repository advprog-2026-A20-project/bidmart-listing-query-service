# SOLID Review - Listing Query Service

## SRP - Single Responsibility Principle

- Controller tetap menangani request/response HTTP melalui `ListingQueryController`.
- `ListingQueryService` kini fokus pada orchestration use case.
- `ListingRequestValidator` menangani validasi input.
- `ListingFactory` menangani pembuatan dan update entity listing.
- `ListingReadModelAssembler` menangani read model, display price, status efektif, dan total bid.
- `ListingResponseMapper` menangani mapping DTO.

## OCP - Open/Closed Principle

- Rule lifecycle ditambahkan lewat state/policy, bukan conditional tersebar.
- Filter listing memakai `ListingSpecificationStrategy`; filter baru bisa ditambah sebagai class baru.
- Mapping response dipusatkan di mapper sehingga perubahan DTO tidak menyentuh orchestration service.

## LSP - Liskov Substitution Principle

- Semua implementasi `ListingState` memenuhi kontrak `canEdit()` dan `canReceiveBid()`.
- Semua implementasi `ListingSpecificationStrategy` bisa diganti/dikombinasi oleh `ListingSpecificationBuilder`.

## ISP - Interface Segregation Principle

- `ListingState` hanya berisi operasi lifecycle yang dibutuhkan.
- `ListingSpecificationStrategy` hanya berisi operasi pembuatan `Specification`.
- Tidak ada interface besar yang memaksa implementasi method tidak relevan.

## DIP - Dependency Inversion Principle

- `ListingSpecificationBuilder` bergantung pada interface `ListingSpecificationStrategy`.
- `ListingQueryService` bergantung pada komponen application-layer yang diinjeksi via constructor.
- Tidak ada instansiasi dependency concrete dengan `new` di service utama.

## Status

- SRP: Fixed
- OCP: Fixed
- LSP: OK
- ISP: OK
- DIP: OK
