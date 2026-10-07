# API Dependency Analysis

Controller → service → repository/service call chains and named service-to-service coupling, based on constructor-injected fields read directly from source (`private final` fields in each `*ServiceImpl`).

## Most heavily coupled service classes (ranked)

### 1. `TempSellerServiceImpl` (`service/serviceImpl/temp/seller/TempSellerServiceImpl.java`) — heaviest in the codebase
Injects **11 repositories + 4 services**:
`TempSellerRepository`, `ProductTypeMasterRepository`, `CompanyTypeMasterRepository`, `SellerTypeMasterRepository`, `StateMasterRepository`, `DistrictMasterRepository`, `TalukaMasterRepository`, `DocumentTypeMasterRepository`, `TempSellerDocumentRepository`, `TempSellerBankDetailsRepository`, `SellerRepository`, `UserRepository`, and `RequestIdGeneratorService`, `S3Service`, `SellerTypeFieldValidator`, `IndependentEmailService`.
This single class spans the entire seller-registration-wizard workflow: request-ID generation, master-data lookups for 7 different reference tables, document upload (S3), promotion into the real `Seller`/`User` tables, and field validation.

### 2. `SellerServiceImpl` (`service/seller/sellerImpl/SellerServiceImpl.java`)
Injects: `SellerRepository`, `CompanyTypeMasterRepository`, `SellerTypeMasterRepository`, `ProductTypeMasterRepository`, `StateMasterRepository`, `DistrictMasterRepository`, `TalukaMasterRepository`, `PasswordEncoder`. 7 repositories — same master-data fan-out pattern as `TempSellerServiceImpl`, confirming Seller and Master/Reference are the tightest cross-domain pairing in the system.

### 3. `SellerApprovalServiceImpl` (`service/serviceImpl/admin/SellerApprovalServiceImpl.java`)
Injects: `TempSellerRepository`, `SellerRepository`, `EmailService`, `PdfService`, `SellerTermsRepository`, `TempSellerReviewHistoryRepository`, `S3Service`, `ProductTypeMasterRepository`, and **`TempSellerService`** (a service, not just a repo). This is the admin approval bridge that promotes a `TempSeller` to a real `Seller` — it is the one place today where the "Seller & Onboarding" service boundary proposed in `microservice-boundaries.md` is naturally already a single transaction; keeping it that way avoids a distributed-transaction rewrite.

### 4. `OrderPlacementServiceImpl` (`service/order/orderImpl/OrderPlacementServiceImpl.java`)
Injects: `BuyerRepository`, `BuyerDeliveryAddressRepository`, `PricingDetailsRepository`, `OrderRepository`, `PaymentRepository`, `QuoteRequestRepository`, `StockService`, `OrderMapper`, `OrderNotificationService`. Reaches directly into Buyer (2 repos) and Product (`PricingDetailsRepository`, `StockService`) domains from within the Order service — the clearest evidence that "place order" is a cross-domain transaction today (see `database-ownership.md` and `resilience-architecture.md`).

### 5. `ReturnRefundServiceImpl` (`service/order/orderImpl/ReturnRefundServiceImpl.java`)
Injects: `OrderItemRepository`, `BuyerRepository`, `ReturnRequestRepository`, `RefundRepository`, `SellerOrderRepository`, `OrderRepository`, `StockService`. Same Buyer+Product reach-through as OrderPlacementServiceImpl, for the return/refund flow.

### 6. `SellerOrderFulfillmentServiceImpl`
Injects: `SellerOrderRepository`, `OrderRepository`, `OrderMapper`, `OrderNotificationService`, `TwilioOTPService`, `InvoiceService`. Notably calls `TwilioOTPService` (a class that otherwise lives entirely in the temp-seller-phone-OTP domain) — an SMS-for-OTP utility being reused generically for fulfillment notifications, i.e. it's really acting as a generic SMS sender, not an OTP-specific service, and is misplaced under `service/temp/seller`.

### 7. `OrderCancellationServiceImpl`
Injects: `OrderRepository`, `SellerOrderRepository`, `RefundRepository`, `StockService`, `OrderMapper`, `OrderNotificationService`. Product-domain reach via `StockService` again.

### 8. `BuyerApprovalServiceImpl`
Injects: `TempBuyerRepository`, `BuyerRepository`, `EmailService`, `TempBuyerReviewHistoryRepository`, `S3Service` — the buyer-side mirror of `SellerApprovalServiceImpl`, same "promotion" pattern, one transaction spanning TempBuyer → Buyer.

## Controller → service chains (representative, by domain)

- **`controller/temp/seller/TempSellerController`** → `TempSellerService` (impl above) → 11 repos. Endpoints cover create/update draft seller, submit for review, upload documents (delegated to `TempSellerDocumentServiceImpl`, itself injecting `TempSellerRepository`, `TempSellerDocumentRepository`, `TempSellerCoordinatorRepository`, `S3Service`).
- **`controller/seller/profile/AdminSellerApprovalController`** → `SellerApprovalService` (impl above) — approve/reject endpoints; approve path calls `PdfService` (approval letter) and `EmailService` (notification) in addition to the repo writes.
- **`controller/order/OrderController`** → `OrderPlacementService`/`OrderQueryService`/`OrderCancellationService` depending on endpoint — each a distinct interface/impl pair, but all sharing `OrderRepository` and `OrderMapper`.
- **`controller/order/SellerOrderController`** → `SellerOrderFulfillmentService` → (as above) reaches into `InvoiceService` for invoice generation and `TwilioOTPService` for delivery notifications.
- **`controller/order/PaymentController`** → `PaymentService` → `PaymentServiceImpl` (`PaymentRepository` only — the *least* coupled service in the Order domain, a good extraction-internal seam if Order were ever split further).
- **`controller/order/InvoiceController`** → `InvoiceService` → `InvoiceServiceImpl` (`SellerOrderRepository`, `InvoiceRepository`, `S3Service` — generates and stores PDF invoices via `PdfService`/`S3Service`).
- **`controller/order/ReturnController`** → `ReturnRefundService` (impl above).
- **`controller/product/ExcelProductImportController` / `ProductImportController`** → `ExcelProductImportService`/`UniversalExcelImportService` → `ProductImportStrategyFactory` → one of `DrugImportStrategy`/`ConsumableImportStrategy`/`NonConsumableImportStrategy`/`CosmeticsImportStrategy`/`FoodInfantImportStrategy`/`SupplementsImportStrategy`, each of which writes to `ProductDetailsRepository` plus its own attribute-table repository and the relevant master-lookup repositories (dosage form, molecule, GST%, etc.) — this fan-out is internal to Product Catalog, not cross-domain, so it is safe to keep as one service.
- **`controller/admin/AdminSellerController`/`AdminBuyerController`/`AdminOrderController`** → thin pass-throughs to the approval/order services above; these controllers are not a separate domain, just an operational surface.
- **Master data controllers** (`controller/master/*MasterController`, 8 of them) → one `*MasterService`/`*MasterServiceImpl` pair each, each wrapping exactly one repository — the least coupled, most CRUD-boilerplate part of the codebase (confirms the "keep as shared reference module" recommendation in `microservice-boundaries.md`).

## Repository-level cross-entity joins (`@Query`)

22 repository files contain custom `@Query` methods (see list from Glob/Grep pass): `LoginOtpRepository`, `UserRepository`, `BuyerLoginOtpRepository`, `BuyerRepository`, `BuyerUserRepository`, `InvoiceRepository`, `OrderRepository`, `PaymentRepository`, `MoleculeRepository`, `PackagingDetailsRepository`, `PricingDetailsRepository`, `ProductAttributeDrugRepository`, `ProductDetailsRepository`, `ProductImageRepository`, `StockLedgerRepository`, `StorageConditionMasterRepository`, `SellerUserRepository`, `SellerRepository`, `TempBuyerDocumentRepository`, `TempBuyerRepository`, `TempSellerDocumentRepository`, `TempSellerRepository`. Their concentration in Order (`OrderRepository`, `InvoiceRepository`, `PaymentRepository`) and Product (`ProductDetailsRepository`, `PricingDetailsRepository`, `StockLedgerRepository`) confirms those two domains have the most complex query needs and will need the most careful API design (pagination/filter parity) when direct repository access is no longer available across a service boundary.

## Summary: single most-coupled service class

**`TempSellerServiceImpl`** is the most heavily coupled class found: **11 injected repositories + 4 injected services** (`RequestIdGeneratorService`, `S3Service`, `SellerTypeFieldValidator`, `IndependentEmailService`), spanning the Master/Reference domain (7 lookup repos), the Seller domain (`SellerRepository`), Identity (`UserRepository`), and its own temp-seller subtree. This is the class most likely to need decomposition (not necessarily service-extraction) before any split — e.g. separating "load reference data for the form" from "persist/validate/promote" responsibilities.
