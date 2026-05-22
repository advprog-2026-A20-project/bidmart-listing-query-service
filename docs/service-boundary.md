# Listing Query Service Boundary

## Tanggung Jawab

- Membaca public listing catalog.
- Membaca listing detail.
- Membaca kategori dan category tree.
- Menampilkan ringkasan auction pada listing bila data tersedia.

## Tidak Ditangani

- Create listing.
- Update listing.
- Cancel listing.
- Validasi command bidding yang membutuhkan keputusan transactional.
- Wallet, auth, atau notification.

## Dependency

Fase awal masih membaca database yang sama secara read-only. Target berikutnya adalah projection listing yang diisi dari event listing dan bidding.

## Catatan Migrasi

Endpoint publik tetap lewat gateway. Frontend tidak boleh memanggil service ini langsung.
