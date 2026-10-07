# 02 — Business Domain → Target Service Ownership

Which service **owns** (has write authority over) each table group, based on business capability rather than current package layout. One writer per table; every other service reads through an API, not a direct query.

| Domain | Tables Owned | Target Service |
|---|---|---|
| Identity & Access | `tbl_user`, `tbl_role_master`, `tbl_signup_otp`, `tbl_login_otp`, `tbl_refresh_tokens`, `tbl_buyer_user`, `tbl_buyer_login_otp`, `tbl_buyer_refresh_tokens`, `tbl_buyer_signup_otp` | **Identity Service** |
| Seller | `tbl_seller`, `tbl_seller_address`, `tbl_seller_bank_details`, `tbl_seller_coordinator`, `tbl_seller_gst`, `tbl_seller_document`, `tbl_seller_history`, `tbl_pending_seller`, `tbl_pending_seller_document`, `tbl_temp_seller*`, `phone_otp`, `tbl_terms_master`, `tbl_ifsc_overrides` | **Seller Service** |
| Buyer | `tbl_buyer`, `tbl_buyer_address`, `tbl_buyer_contact`, `tbl_buyer_document`, `tbl_buyer_delivery_address`, `tbl_temp_buyer*` | **Buyer Service** |
| Product Catalog | `tm_product_details` + 6 attribute tables, images/manuals/certs, category tree, pricing/discount/schemes, `tbl_stock_ledger`, packaging tables, and all ~25 flat master/lookup tables | **Product Service** |
| Order & Fulfillment | `tbl_order`, `tbl_order_item`, `tbl_seller_order`, `tbl_order_status_history`, `tbl_payment`, `tbl_refund`, `tbl_return_request`, `tbl_invoice`, `tbl_quote_request` | **Order Service** |
| Shared Reference | `tbl_state_master`, `tbl_district_master`, `tbl_taluka_master`, `tbl_company_type_master`, `tbl_seller_type_master`, `tbl_buyer_type_master`, `tbl_document_type_master`, `tbl_product_type_master` | **Reference Data Service** (or cached read-replica inside each consumer — see decision below) |
| Uncategorized | `tbl_legal_content`, `employees` | Owner TBD — neither has FKs into the marketplace domain; recommend Identity Service for `tbl_legal_content` (static content) and leaving `employees` out of this migration entirely until its business purpose is confirmed |

## Why Product owns Pricing, Stock and Master Data (not separate services)

Pricing, stock, and packaging are lifecycle-bound to a product record (`tm_product_details`) — every write path goes through product creation/update. Splitting them into their own services would add cross-service calls for every product read with no independent scaling or ownership benefit. Master/lookup tables (dosage form, GST %, molecule, etc.) are read-only reference data consumed exclusively by Product's import/validation logic (`ProductImportStrategy` implementations) — they stay with Product rather than becoming their own service.

## Why Seller/Buyer each keep their staging ("Temp") tables

`TempSeller*`/`TempBuyer*` are not independent data — they are the pre-approval state of the same aggregate (`SellerApprovalServiceImpl`/`BuyerApprovalServiceImpl` promote temp rows into live rows in one transaction today). Splitting staging into a separate service would break that promotion step across a network boundary for no benefit. They stay inside Seller Service / Buyer Service as an internal staging schema.

## Decision needed: Reference Data

Two viable options, either is defensible — pick one before implementation starts:
1. **Reference Data Service** — one small service, all consumers call it, changes propagate immediately, but adds a network hop to every seller/buyer address form.
2. **Local cached copies** — each consuming service (Seller, Buyer, Product) keeps a read-only local copy refreshed on a schedule or via change events. Faster reads, but data can be briefly stale after a master-data update.

Recommendation: local cached copies, since state/district/taluka/document-type data changes rarely and read latency on onboarding forms matters more than immediate consistency.
