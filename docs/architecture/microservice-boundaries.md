# Proposed Microservice Boundaries

Grounded in the FK graph and service-injection coupling observed in `domain-analysis.md` and `api-dependency-analysis.md`. The goal is **coarse-grained** services — not one per entity/domain package — because the entity graph is densely interconnected (Product ↔ Seller ↔ Order ↔ Buyer all share direct FKs), and over-splitting would just move today's in-process joins into chatty synchronous network calls.

## Recommended: 5 services + 1 shared reference-data module

### 1. Identity & Access service
**Owns:** `tbl_user`, `tbl_role_master`, `tbl_signup_otp`, `tbl_login_otp`, `tbl_refresh_tokens` (seller-side auth: `entity/auth/*`), plus the seller-login-specific tables reachable only through `SellerUserRepository`/`refreshTokenRepository` under `seller/SellerLogIn`, and buyer's parallel identity tables `tbl_buyer_user`, `tbl_buyer_login_otp`, `tbl_buyer_refresh_tokens`, `tbl_buyer_signup_otp`.
**Rationale:** JWT issuing/validation (`JwtUtils`, `AuthTokenFilter`) is already a self-contained cross-cutting concern with no entity FKs into Product/Order. The codebase already has **three parallel auth stacks** (auth/User, seller/SellerLogIn, buyer/BuyerUser) that do near-identical work (signup OTP → password → JWT) — consolidating them behind one service is both a coupling win and a maintenance win, not just an extraction convenience.
**Caution:** `Seller.user` is `@OneToOne` to `User` and `Buyer.buyer_user` is `@OneToOne` to `BuyerUser` — the owning Seller/Buyer service will need the identity service's user id (already stored as a plain FK column) rather than a JPA join; this is a low-risk seam because it's already a nullable/unique FK, not a shared aggregate root.

### 2. Seller & Onboarding service
**Owns:** `tbl_seller` + all `@OneToOne`/`@OneToMany` children (address, bank details, coordinator, GST, document), `tbl_seller_history`, and the entire temp/staging seller subtree (`tbl_temp_seller*`, `phone_otp`, `tbl_terms_master`), plus `tbl_pending_seller`/`tbl_pending_seller_document`.
**Rationale:** This is the most transactionally cohesive cluster in the codebase. `TempSellerServiceImpl` alone injects **10 repositories + 4 services** (`TempSellerRepository`, `ProductTypeMasterRepository`, `CompanyTypeMasterRepository`, `SellerTypeMasterRepository`, `StateMasterRepository`, `DistrictMasterRepository`, `TalukaMasterRepository`, `DocumentTypeMasterRepository`, `TempSellerDocumentRepository`, `TempSellerBankDetailsRepository`, `SellerRepository`, `UserRepository`, plus `RequestIdGeneratorService`, `S3Service`, `SellerTypeFieldValidator`, `IndependentEmailService`) — the registration wizard, its validation, and its eventual promotion to a real `Seller` are one workflow and should stay one deployable. `SellerApprovalServiceImpl` (admin bridge) reads `TempSellerRepository`/`TempSellerReviewHistoryRepository` and writes `SellerRepository` in the same transaction today — splitting temp-seller and seller into different services would turn this single-transaction promotion into a distributed transaction for no architectural benefit.
**Depends on (via API, post-split):** Master/Reference service (state/district/taluka/company-type/seller-type/document-type/product-type lookups — read-only, cacheable), Identity service (create `User` on approval), S3/notification for document upload and coordinator emails (already abstracted behind `S3Service`/`EmailService`, so this is a low-friction seam already).

### 3. Buyer & Onboarding service
**Owns:** `tbl_buyer` + children (address, contact, document, delivery address), and the buyer temp/staging subtree (`tbl_temp_buyer*`).
**Rationale:** Mirrors the Seller service's structure and coupling shape (`BuyerApprovalServiceImpl` bridges `TempBuyerRepository` → `BuyerRepository` the same way `SellerApprovalServiceImpl` does). Kept as a **separate** service from Seller (not merged) because today they share almost no FKs — the only cross-links are through shared Master/Reference tables (state/district/taluka, document-type) and, downstream, through Order (`tbl_order.buyer_id`) and Quote — all of which are natural service-boundary calls even in the current monolith.

### 4. Product Catalog service
**Owns:** `tm_product_details` and its full attribute-table family (drug, consumable/non-consumable medical, cosmetic, food/infant, supplements), pricing/discount/scheme tables, packaging tables, stock ledger, and the ~25 product-specific master/lookup tables (dosage form, molecule, GST%, device category tree, certification, storage condition, etc.), plus the Excel/CSV bulk-import strategy classes.
**Rationale:** This is the largest, most self-contained entity cluster (~50 of ~117 entities) and the import-strategy pattern (`ProductImportStrategyFactory` + one strategy per category) already treats "product category" as the natural internal seam, so it doesn't need to be split further. The one real external FK is `tm_product_details.seller_id` → `tbl_seller`, and `tbl_stock_ledger.seller_id` — both become API calls to the Seller service (or a denormalized `seller_id` kept as an opaque foreign key, validated at write time via API, the same way the `TalukaMaster`/`DistrictMaster` FKs already use `insertable=false, updatable=false` read-only join columns alongside a plain `state_id` column).
**Depends on (via API):** Seller service (validate `seller_id` on product create/update), Order service (stock decrement on order placement — see #5).

### 5. Order, Payment & Fulfillment service
**Owns:** `tbl_order`, `tbl_order_item`, `tbl_seller_order`, `tbl_order_status_history`, `tbl_payment`, `tbl_refund`, `tbl_return_request`, `tbl_invoice`, and (recommend moving in) `tbl_quote_request` since `QuoteRequest` has the same three FK shape (product, seller, buyer_user) as Order and is a precursor workflow to it.
**Rationale:** Already the best-decomposed domain internally (7 single-purpose service interfaces + shared `OrderMapper`/`OrderNotificationService`/`OrderStatusRollup` support classes) — that internal seam quality is exactly what should be preserved by keeping it one deployable rather than fragmenting `OrderPlacementService`/`OrderQueryService`/etc. into separate processes.
**Named coupling to resolve at the boundary:** `OrderPlacementServiceImpl` injects `BuyerRepository`, `BuyerDeliveryAddressRepository` (Buyer service), `PricingDetailsRepository`, `StockService` (Product service) directly alongside its own `OrderRepository`/`PaymentRepository`/`QuoteRequestRepository`. `ReturnRefundServiceImpl` and `OrderCancellationServiceImpl` also both call `StockService` directly (to restore/decrement stock). Post-split, every one of these becomes a synchronous API call (or, per `event-architecture.md`, an async event) to Buyer and Product services — this is the highest-risk boundary in the whole system and needs saga/compensation design (see `resilience-architecture.md`) because "place order" today is a single local `@Transactional` spanning Buyer, Product/Stock and Order tables.

### Shared Master/Reference module — **recommend NOT extracting as its own service**
`tbl_state_master`, `tbl_district_master`, `tbl_taluka_master`, `tbl_company_type_master`, `tbl_seller_type_master`, `tbl_buyer_type_master`, `tbl_document_type_master`, `tbl_product_type_master`, `tbl_role_master`.
**Rationale for keeping monolithic (or a thin shared library, not a network service):** these are near-static, low-write reference tables read by almost every other domain (Seller, TempSeller, Buyer, TempBuyer, Product, Admin approval). Turning every state/district/taluka lookup into a network hop would add latency and a new failure mode to registration forms and address entry with no offsetting benefit — they change rarely and are natural candidates for read replication/caching (each owning service can hold a local read-only cache refreshed from a single source of truth) rather than a dedicated microservice. If a boundary is forced later, package this as a **versioned reference-data service with aggressive client-side caching**, not a synchronous-call dependency in hot paths.

### Legal Content / IFSC Override / Employee
Each is a small standalone table with no FKs into the rest of the schema (`LegalContent`, `IFSCOverride`, `Employee`). **Recommendation: keep in the monolith / fold into whichever service is operationally convenient** (e.g. Legal Content as a static-content endpoint inside Identity & Access; IFSC Override alongside Seller since it's only consumed by seller bank-detail forms; Employee is unrelated to the marketplace domain entirely and its ownership should be confirmed with the team — ASSUMPTION: it is an internal admin/staff record not exposed to sellers/buyers, based on the table name and its isolation from every other entity).

## Explicitly kept monolithic

- **Master/Reference data** (see above) — extraction would add coupling risk (chatty reads) without matching write-side justification.
- **Seller ↔ Temp Seller ↔ Pending Seller** — one service, not three, because the promotion flow (`SellerApprovalServiceImpl`) is a single local transaction today across `TempSeller*` and `Seller*` tables, and forcing that across a network boundary means introducing a distributed transaction / saga purely to satisfy a service-per-entity aesthetic.
- **Order's 7 internal service interfaces** stay one deployable — they are already well-factored internally (single responsibility per interface); the coupling that matters is *external* (Buyer, Product), not internal.

## Summary table

| # | Service | Entity/table count (approx) | Key external dependency after split |
|---|---|---|---|
| 1 | Identity & Access | 8 (auth + buyer auth tables) | none upstream; everyone downstream depends on it |
| 2 | Seller & Onboarding | ~20 (seller + temp-seller + pending-seller) | Master/Reference (cached), Identity, S3/Email |
| 3 | Buyer & Onboarding | ~14 (buyer + temp-buyer) | Master/Reference (cached), Identity |
| 4 | Product Catalog | ~50 (product + all attribute/master tables) | Seller (seller_id validation) |
| 5 | Order, Payment & Fulfillment (+Quote) | ~9 | Buyer, Product/Stock, Seller |
| — | Master/Reference (shared library or lightweight service, not split further) | 9 | none |
| — | Misc (Legal Content, IFSC, Employee) | 3 | fold into an existing service, do not extract standalone |

This lands at **5 coarse-grained services**, which fits the requested 3–7 range and matches the actual coupling clusters found, rather than the ~13 domain packages `domain-analysis.md` enumerates.
