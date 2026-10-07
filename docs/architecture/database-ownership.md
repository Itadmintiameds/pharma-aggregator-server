# Database Ownership Post-Split

Table-level ownership mapping for the 5-service boundary proposed in `microservice-boundaries.md`, plus how the cross-domain FKs found in `entity/**` would need to change.

## Ownership by proposed service

### Identity & Access
`tbl_user`, `tbl_role_master`, `tbl_signup_otp`, `tbl_login_otp`, `tbl_refresh_tokens`, `tbl_buyer_user`, `tbl_buyer_login_otp`, `tbl_buyer_refresh_tokens`, `tbl_buyer_signup_otp`.

### Seller & Onboarding
`tbl_seller`, `tbl_seller_address`, `tbl_seller_bank_details`, `tbl_seller_coordinator`, `tbl_seller_gst`, `tbl_seller_document`, `tbl_seller_history`, `tbl_temp_seller`, `tbl_temp_seller_address`, `tbl_temp_seller_bank_details`, `tbl_temp_seller_coordinator`, `tbl_temp_seller_document`, `tbl_temp_seller_review_history`, `tbl_temp_seller_email_otp`, `phone_otp`, `tbl_terms_master`, `tbl_pending_seller`, `tbl_pending_seller_document`.
(Also recommend it own `tbl_ifsc_overrides` — only consumed by seller bank-detail entry.)

### Buyer & Onboarding
`tbl_buyer`, `tbl_buyer_address`, `tbl_buyer_contact`, `tbl_buyer_document`, `tbl_buyer_delivery_address`, `tbl_temp_buyer`, `tbl_temp_buyer_address`, `tbl_temp_buyer_contact`, `tbl_temp_buyer_document`, `tbl_temp_buyer_review_history`.

### Product Catalog
`tm_product_details`, `tm_product_attribute_drug` (+ its `@OneToOne`/`@OneToMany` children), `tm_product_attribute_consumable_medical`, `tm_product_attribute_non_consumable_medical`, `tm_product_attribute_cosmetic_and_personal_use`, `tm_product_attribute_food_infant`, `tm_product_attribute_supplements_or_nutraceuticals`, `tm_product_image`, `tm_product_user_manual`, `tm_product_certificate_document`, `tm_pricing_details`, `tm_additional_discount`, `tm_special_schemes`, `tbl_stock_ledger`, `tm_pack_type`, `tm_pack_type_unit_master`, `tm_packaging_details`, `tm_category`, `tm_product_category_master`, `tm_product_subcategory_master`, `tm_therapeutic_category_master`, `tm_therapeutic_subcategory_master`, `tbl_drug_categories_master`, and all ~25 flat master tables (`tbl_dosage_form_master`, `tm_flavour_master`, `tm_gst_percentage_master`, `tbl_molecules_master`, `tm_molecule_strength_format`, `tbl_net_quantity_unit_master`, `tbl_serving_size_unit_master`, `tm_strength_unit`, `tbl_storage_condition_master`, `tbl_device_category_master`, `tbl_device_sub_category_master`, `tbl_device_specification_unit_master`, `tbl_consumable_material_type_master`, `tbl_non_consumable_material_type_master`, `tbl_power_source_master`, `tbl_country_master`, `tbl_certification_master`, `tm_certificate_document`, `tbl_diamension_size_master`, `tm_age_group_master`, `tm_hair_type_master`, `tm_skin_type_master`, `tm_intended_use_area_master`, `tbl_product_form_master`, `tm_product_form_master`).

### Order, Payment & Fulfillment
`tbl_order`, `tbl_order_item`, `tbl_seller_order`, `tbl_order_status_history`, `tbl_payment`, `tbl_refund`, `tbl_return_request`, `tbl_invoice`, `tbl_quote_request`.

### Shared / Master-Reference (kept as a lightweight shared module or heavily-cached read service, not split into the 5)
`tbl_state_master`, `tbl_district_master`, `tbl_taluka_master`, `tbl_company_type_master`, `tbl_seller_type_master`, `tbl_buyer_type_master`, `tbl_document_type_master`, `tbl_product_type_master`.

### Ungrouped / needs owner decision
`tbl_legal_content` (no FKs — recommend Identity & Access, as generic static content), `employees` (no FKs to anything else — ASSUMPTION: internal staff table, owner TBD by the team, out of scope of the buyer/seller marketplace split).

## Cross-domain FKs and how they'd be handled post-split

| FK today (entity → target) | From service | To service | Post-split handling |
|---|---|---|---|
| `Seller.user` → `tbl_user` | Seller & Onboarding | Identity & Access | Keep as opaque `user_id` column (already `@JoinColumn(name="user_id", unique=true)`); Seller service calls Identity's `/users/{id}` for auth checks, does not join. |
| `Buyer.buyer_user` → `tbl_buyer_user` | Buyer & Onboarding | Identity & Access | Same pattern — opaque FK column, API call for validation. |
| `TempSeller.companyTypeId/sellerTypeId/state/district/taluka` → master tables | Seller & Onboarding | Master/Reference | Keep FK columns as plain IDs (no DB-level FK constraint across services); cache master data locally in Seller service, refreshed periodically or via change events. |
| `TempSellerDocument.productTypeId/documentTypeId`, `SellerDocument.productTypeId/documentTypeId` | Seller & Onboarding | Master/Reference | Same as above. |
| `Buyer.buyerType` → `tbl_buyer_type_master` (which itself FKs `tbl_document_type_master`) | Buyer & Onboarding | Master/Reference | Same as above. |
| `ProductDetails.seller_id` → `tbl_seller` | Product Catalog | Seller & Onboarding | **Highest-risk FK.** No more DB foreign key; Product service validates `seller_id` via a synchronous API call at product-create time and stores it as an opaque column. Product listing pages that today `JOIN` to seller for display data (name, address) must instead call Seller service or maintain a denormalized read-model (seller name/city) updated via the event pattern in `event-architecture.md`. |
| `StockLedger.seller_id` → `tbl_seller` | Product Catalog | Seller & Onboarding | Same as above. |
| `Order.buyer_id` → `tbl_buyer` | Order & Fulfillment | Buyer & Onboarding | Opaque FK; Order service calls Buyer service for delivery-address resolution (`BuyerDeliveryAddressRepository` currently injected directly into `OrderPlacementServiceImpl` — becomes an API call). |
| `SellerOrder.seller_id` → `tbl_seller` | Order & Fulfillment | Seller & Onboarding | Opaque FK; used for seller-side order dashboards — either API call or a denormalized projection. |
| `OrderItem.pricing_id`/`OrderPlacementServiceImpl`'s `PricingDetailsRepository` | Order & Fulfillment | Product Catalog | Price lookup at order time becomes a synchronous API call (price must be snapshotted into `OrderItem` at write time regardless — check whether `OrderItem` already stores a price snapshot or only the FK; if only the FK, this must change before splitting, since a live join to Product's price table is not available across a service boundary). |
| `ReturnRefundServiceImpl`/`OrderCancellationServiceImpl` → `StockService` | Order & Fulfillment | Product Catalog | Stock adjustments on cancel/return become an async event (stock decrement/restore) rather than an in-process call — see `event-architecture.md`. This is the clearest case for eventual consistency instead of a synchronous call, since stock correctness has some tolerance for eventual settlement but order/return UX should not block on Product service availability. |
| `QuoteRequest` → product_id, seller_id, buyer_user_id | Order & Fulfillment (if Quote moves there) | Product Catalog, Seller, Identity | Three opaque FKs; quote creation validates each via API. |

## General pattern

Every cross-domain FK becomes: (a) an opaque ID column with no DB-enforced referential integrity, (b) a synchronous validating API call at write time, and (c) for read-heavy display joins, either a client-side aggregation call or a denormalized/cached projection kept eventually consistent via the event pattern proposed in `event-architecture.md`. Master/Reference tables should be cached locally in each consuming service rather than called synchronously on every request, since they are read-heavy and near-static.
