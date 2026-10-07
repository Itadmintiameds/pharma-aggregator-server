# Domain Analysis

Business domains as actually reflected in package/entity naming (`entity/**`, `controller/**`, `service/**`), not an idealized DDD carve-up.

## 1. Auth / Identity (`entity/auth`, `controller/auth`, `service/auth`, `security/*`)

Tables: `tbl_user` (User — roles via `@ManyToMany` to `tbl_role_master`), `tbl_signup_otp`, `tbl_login_otp`, `tbl_refresh_tokens`.
Controllers: `auth/SignupController`.
Services: `SignupService`, `UserCreationService`.
This is the identity backbone used by the **seller** login path (`Seller.user` is `@OneToOne` to `User`) and separately by `Employee`. Buyer has its own parallel identity table (`BuyerUser`) — buyer auth is NOT built on `tbl_user`.

## 2. Seller (approved) (`entity/seller`, `controller/seller`, `service/seller`)

Core table `tbl_seller` (`Seller`), with `@OneToOne` children `tbl_seller_address`, `tbl_seller_bank_details`, `tbl_seller_coordinator`, `tbl_seller_gst`, `@OneToMany` `tbl_seller_document`, `@ManyToMany` to `tbl_product_type_master`, `@ManyToOne` to `tbl_company_type_master`/`tbl_seller_type_master`, and `@OneToOne` to `tbl_user`.
Sub-domain **seller login**: `controller/seller/SellerLogIn/{AuthController, AuthenticationController}`, `service/seller/SellerLogIn/{AuthService, UserService, PasswordResetEmailService}`, `repository/seller/SellerLogIn/{SellerUserRepository, refreshTokenRepository}` — a *second*, seller-specific auth stack parallel to `entity/auth`.
Sub-domain **seller profile/approval**: `entity/seller/profile/{PendingSeller, PendingSellerDocument}` (tables `tbl_pending_seller`, `tbl_pending_seller_document`), `controller/seller/profile/{AdminSellerApprovalController, SellerProfileController}`, `service/profile/*`.
Sub-domain **seller history**: `entity/seller/history/SellerHistory` (`tbl_seller_history`), `service/seller/history/SellerHistoryService`.

## 3. Temp/Staging Seller Onboarding (`entity/temp/seller`, `controller/temp/seller`, `service/*/temp/seller`)

This is the **seller registration wizard staging area** — a full shadow copy of the seller schema used before admin approval promotes data into the real `seller` domain: `tbl_temp_seller` (`TempSeller`), `tbl_temp_seller_address`, `tbl_temp_seller_bank_details`, `tbl_temp_seller_coordinator`, `tbl_temp_seller_document`, `tbl_temp_seller_review_history`, `tbl_temp_seller_email_otp`, `phone_otp` (`PhoneOTP`), `tbl_terms_master` (`SellerTerms`).
Controllers: `temp/seller/{TempSellerController, TempSellerEmailOtpController, SMSOTPController, OnSubmit/IndependentEmailController}`.
Services: `TempSellerServiceImpl` (heaviest-coupled class in the codebase — see `api-dependency-analysis.md`), `TempSellerDocumentServiceImpl`, `SellerTypeFieldValidator`, `TwilioOTPService`, `RequestIdGeneratorService`, `IndependentEmailService`.
The **admin approval bridge** lives in `service/serviceImpl/admin/SellerApprovalServiceImpl`, which reads from `TempSellerRepository`/`TempSellerReviewHistoryRepository` and writes into `SellerRepository` — i.e. approval is the one place temp-seller and seller domains are transactionally joined today.

## 4. Buyer (`entity/buyer`, `controller/buyer`, `service/buyer`)

Core table `tbl_buyer` (`Buyer`, `@ManyToOne` to `tbl_buyer_type_master`, `@OneToOne` to `tbl_buyer_user`), children `tbl_buyer_address`, `tbl_buyer_contact`, `tbl_buyer_document`, `tbl_buyer_delivery_address` (multiple, `@OneToMany`). Separate identity table `tbl_buyer_user` (`BuyerUser`) with its own `tbl_buyer_login_otp`, `tbl_buyer_refresh_tokens`, `tbl_buyer_signup_otp` — buyer auth is fully independent of the `auth`/seller-login stacks (three parallel auth subsystems exist in this codebase).
Controllers: `buyer/{BuyerAuthenticationController, BuyerProfileController, BuyerSignupController}`.
Services: `BuyerAuthService`, `BuyerProfileService`, `BuyerSignupService`.

## 5. Temp/Staging Buyer Onboarding (`entity/temp/buyer`)

Mirrors the temp-seller pattern: `tbl_temp_buyer`, `tbl_temp_buyer_address`, `tbl_temp_buyer_contact`, `tbl_temp_buyer_document`, `tbl_temp_buyer_review_history`.
Controller: `temp/buyer/TempBuyerController`. Admin bridge: `service/serviceImpl/admin/BuyerApprovalServiceImpl` (TempBuyerRepository → BuyerRepository, same pattern as seller approval).

## 6. Admin (approvals only — no admin UI in this repo per frontend `CLAUDE.md`) (`controller/admin`, `service/admin`)

`AdminSellerController`, `AdminBuyerController`, `AdminOrderController` — thin controllers delegating to `SellerApprovalServiceImpl`/`BuyerApprovalServiceImpl`/order services. This is not a separate data domain; it's an operation surface over Seller/Buyer/Order domains.

## 7. Product Catalog & Master Data (`entity/product`, huge — ~50 entity files, `controller/product`, `service/product`)

The largest domain by entity count. Structure:
- **Category tree**: `tm_category` (`Category`) → `tm_product_category_master` → `tm_product_subcategory_master`, plus `tm_therapeutic_category_master`/`tm_therapeutic_subcategory_master`, `tbl_drug_categories_master`.
- **Per-category-family attribute tables** (this is the "one entity per product type" pattern the frontend `CLAUDE.md` also documents on its side): `tm_product_attribute_drug`, `tm_product_attribute_consumable_medical`, `tm_product_attribute_non_consumable_medical`, `tm_product_attribute_cosmetic_and_personal_use`, `tm_product_attribute_food_infant`, `tm_product_attribute_supplements_or_nutraceuticals` — each `@ManyToOne` to `tm_product_details` (`product_id`) and to a large number of master tables (dosage form, storage condition, country, certification, device category/sub-category, unit tables, age group, etc.), several via `@ManyToMany` join tables.
- **Core product record**: `tm_product_details` (`ProductDetails`) — `@ManyToOne` to `tm_category` and `tbl_seller` (**cross-domain FK into Seller**), `@OneToMany` (cascade ALL, orphanRemoval) into 8 attribute/child tables (one per product-type attribute entity plus images/docs/manual).
- **Pricing & stock**: `tm_pricing_details` (`PricingDetails`, FK to product + packaging), `tm_additional_discount`, `tm_special_schemes`, `tbl_stock_ledger` (`StockLedger`, FK to pricing + product + **seller**).
- **Packaging**: `tm_pack_type`, `tm_pack_type_unit_master`, `tm_packaging_details`.
- **~25 flat master/lookup tables**: dosage form, flavour, GST %, molecule + strength format, net quantity unit, serving size unit, strength unit, storage condition, device category/sub-category/specification-unit, material types (consumable/non-consumable), power source, country, certification (+certificate document), dimension size, age group, hair/skin type, intended-use area, product form.
- **Bulk import**: `controller/product/{ExcelProductImportController, ProductImportController}`, `service/product/util/{ProductImportStrategy, ProductImportStrategyFactory, DrugImportStrategy, ConsumableImportStrategy, NonConsumableImportStrategy, CosmeticsImportStrategy, FoodInfantImportStrategy, SupplementsImportStrategy}` — a Strategy-pattern importer keyed by product category, one strategy class per attribute table above (matches the frontend `CLAUDE.md`'s note that these line up 1:1 with `src/schema/product/*Schema.ts`).

## 8. Reference/Master Geo & Org Data (`entity/master`, `controller/master`, `service/master`)

`tbl_state_master`, `tbl_district_master` (FK state), `tbl_taluka_master` (FK state+district), `tbl_company_type_master`, `tbl_seller_type_master`, `tbl_buyer_type_master` (FK to `tbl_document_type_master` for mandatory doc), `tbl_document_type_master`, `tbl_product_type_master`, `tbl_role_master`. These are **shared reference tables** consumed by Seller, TempSeller, Buyer, TempBuyer and Product domains alike (state/district/taluka FKs appear in seller address, seller bank details, temp-seller address/bank, buyer address, temp-buyer address, pending-seller).

## 9. Orders / Payments / Fulfillment (`entity/order`, `controller/order`, `service/order`)

`tbl_order` (`Order`, FK `tbl_buyer`) → `tbl_order_item` (FK seller_order, product, pricing) and `tbl_seller_order` (`SellerOrder`, FK order + **seller**, 1:1 to `tbl_invoice`) → `tbl_order_status_history`. `tbl_payment` (1:1 order), `tbl_refund` (FK payment + order item), `tbl_return_request` (FK order item + buyer, 1:1 refund), `tbl_invoice` (1:1 seller_order).
Controllers: `order/{OrderController, SellerOrderController, PaymentController, InvoiceController, ReturnController}`.
Services: split cleanly into single-purpose interfaces (`OrderPlacementService`, `OrderQueryService`, `OrderCancellationService`, `SellerOrderFulfillmentService`, `PaymentService`, `InvoiceService`, `ReturnRefundService`) plus shared support (`OrderMapper`, `OrderNotificationService`, `OrderStatusRollup`, `InvoicePdfResult`) under `service/order/support`. This is the domain with the cleanest internal service decomposition in the codebase, but it reaches heavily into Product (`PricingDetailsRepository`, `StockService`), Buyer (`BuyerRepository`, `BuyerDeliveryAddressRepository`) and Seller (`SellerOrderRepository` joins to seller) — see coupling notes below.

## 10. Quotes (`entity/quote`, `controller/quote`, `service/quote`)

`tbl_quote_request` (`QuoteRequest`) — FKs to product, seller, and buyer_user. Controllers `BuyerQuoteRequestController`/`SellerQuoteRequestController`, single `QuoteRequestService`. Small domain, cross-cutting FKs into Product/Seller/Buyer.

## 11. Legal Content (`entity/content`, `controller/content`)

`tbl_legal_content` (`LegalContent`) — standalone, no FKs. `LegalContentController`/`LegalContentService`. Matches frontend `CLAUDE.md`'s "legal content fetching" feature.

## 12. IFSC Override (`entity/ifsc`)

`tbl_ifsc_overrides` (`IFSCOverride`) — standalone lookup table used by seller bank-detail forms (frontend has `IFSCService.ts`). No entity relationships.

## 13. Employee (misc, top-level `entity/Employee.java`)

`employees` table, its own `EmployeeRepository`/`EmployeeService`/`EmployeeServiceImpl` — an outlier with no visible relationship to any other domain; looks like an internal-staff record unrelated to seller/buyer.

## Domain → owning-service summary table

| Domain | Primary tables (owned) | Primary service classes |
|---|---|---|
| Auth | tbl_user, tbl_signup_otp, tbl_login_otp, tbl_refresh_tokens | SignupService, UserCreationService |
| Seller | tbl_seller, tbl_seller_address/bank/coordinator/gst/document, tbl_seller_history | SellerServiceImpl (seller/sellerImpl), AuthService (seller login), SellerProfileService, SellerHistoryService |
| Temp Seller | tbl_temp_seller* , phone_otp, tbl_terms_master | TempSellerServiceImpl, TempSellerDocumentServiceImpl, TwilioOTPService |
| Buyer | tbl_buyer, tbl_buyer_user, tbl_buyer_address/contact/document/delivery_address, otp/token tables | BuyerAuthService, BuyerProfileService, BuyerSignupService |
| Temp Buyer | tbl_temp_buyer* | TempBuyerServiceImpl, TempBuyerDocumentServiceImpl |
| Admin/Approval | (none owned; reads/writes Seller+TempSeller / Buyer+TempBuyer) | SellerApprovalServiceImpl, BuyerApprovalServiceImpl |
| Product Catalog | tm_product_details + 6 attribute tables + ~25 master tables, pricing/stock/packaging | ProductDetailsServiceImpl, PricingDetailsServiceImpl, StockServiceImpl, ExcelProductImportServiceImpl, UniversalExcelImportService, one *ServiceImpl per master lookup |
| Master/Reference | tbl_state/district/taluka/company_type/seller_type/buyer_type/document_type/product_type/role_master | one *MasterServiceImpl per table under service/serviceImpl/master |
| Orders | tbl_order, tbl_order_item, tbl_seller_order, tbl_order_status_history, tbl_payment, tbl_refund, tbl_return_request, tbl_invoice | OrderPlacementServiceImpl, OrderQueryServiceImpl, OrderCancellationServiceImpl, SellerOrderFulfillmentServiceImpl, PaymentServiceImpl, InvoiceServiceImpl, ReturnRefundServiceImpl |
| Quotes | tbl_quote_request | QuoteRequestService |
| Legal Content | tbl_legal_content | LegalContentService |
| IFSC | tbl_ifsc_overrides | IFSCOverrideService |
| Employee | employees | EmployeeServiceImpl |
