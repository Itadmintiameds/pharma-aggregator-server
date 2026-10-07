# 01 — Table Inventory

Every table in the monolith, grouped by the domain it currently lives under (`entity/**`). This is the source of truth before any service split is decided. Detail on relationships and ownership decisions lives in the other documents in this folder.

## Auth (Seller login identity)
`tbl_user`, `tbl_role_master`, `tbl_signup_otp`, `tbl_login_otp`, `tbl_refresh_tokens`

## Seller
`tbl_seller`, `tbl_seller_address`, `tbl_seller_bank_details`, `tbl_seller_coordinator`, `tbl_seller_gst`, `tbl_seller_document`, `tbl_seller_history`, `tbl_pending_seller`, `tbl_pending_seller_document`

## Seller Onboarding (staging, pre-approval)
`tbl_temp_seller`, `tbl_temp_seller_address`, `tbl_temp_seller_bank_details`, `tbl_temp_seller_coordinator`, `tbl_temp_seller_document`, `tbl_temp_seller_review_history`, `tbl_temp_seller_email_otp`, `phone_otp`, `tbl_terms_master`

## Buyer (own identity stack, independent of Auth above)
`tbl_buyer`, `tbl_buyer_user`, `tbl_buyer_address`, `tbl_buyer_contact`, `tbl_buyer_document`, `tbl_buyer_delivery_address`, `tbl_buyer_login_otp`, `tbl_buyer_refresh_tokens`, `tbl_buyer_signup_otp`

## Buyer Onboarding (staging, pre-approval)
`tbl_temp_buyer`, `tbl_temp_buyer_address`, `tbl_temp_buyer_contact`, `tbl_temp_buyer_document`, `tbl_temp_buyer_review_history`

## Product Catalog
`tm_product_details`, `tm_product_attribute_drug`, `tm_product_attribute_consumable_medical`, `tm_product_attribute_non_consumable_medical`, `tm_product_attribute_cosmetic_and_personal_use`, `tm_product_attribute_food_infant`, `tm_product_attribute_supplements_or_nutraceuticals`, `tm_product_image`, `tm_product_user_manual`, `tm_product_certificate_document`, `tm_category`, `tm_product_category_master`, `tm_product_subcategory_master`, `tm_therapeutic_category_master`, `tm_therapeutic_subcategory_master`, `tbl_drug_categories_master`

## Pricing, Stock & Packaging (part of Product today)
`tm_pricing_details`, `tm_additional_discount`, `tm_special_schemes`, `tbl_stock_ledger`, `tm_pack_type`, `tm_pack_type_unit_master`, `tm_packaging_details`

## Product Master/Lookup Data (~25 flat tables, part of Product today)
`tbl_dosage_form_master`, `tm_flavour_master`, `tm_gst_percentage_master`, `tbl_molecules_master`, `tm_molecule_strength_format`, `tbl_net_quantity_unit_master`, `tbl_serving_size_unit_master`, `tm_strength_unit`, `tbl_storage_condition_master`, `tbl_device_category_master`, `tbl_device_sub_category_master`, `tbl_device_specification_unit_master`, `tbl_consumable_material_type_master`, `tbl_non_consumable_material_type_master`, `tbl_power_source_master`, `tbl_country_master`, `tbl_certification_master`, `tm_certificate_document`, `tbl_diamension_size_master`, `tm_age_group_master`, `tm_hair_type_master`, `tm_skin_type_master`, `tm_intended_use_area_master`, `tbl_product_form_master`, `tm_product_form_master`

## Orders, Payment & Fulfillment
`tbl_order`, `tbl_order_item`, `tbl_seller_order`, `tbl_order_status_history`, `tbl_payment`, `tbl_refund`, `tbl_return_request`, `tbl_invoice`

## Quotes
`tbl_quote_request`

## Shared Reference / Master Data (consumed by Seller, Buyer, Product alike)
`tbl_state_master`, `tbl_district_master`, `tbl_taluka_master`, `tbl_company_type_master`, `tbl_seller_type_master`, `tbl_buyer_type_master`, `tbl_document_type_master`, `tbl_product_type_master`

## Ungrouped
`tbl_legal_content` (no FKs — static content), `tbl_ifsc_overrides` (no FKs — seller bank-detail lookup), `employees` (no FKs — internal staff, unrelated to the marketplace domain)

## Notes
- No Flyway migrations exist yet — schema is Hibernate `ddl-auto` managed. Row counts are not available from code and must be pulled from the actual Postgres instance before sizing migration work.
- Three parallel authentication stacks exist today: `tbl_user` (seller login), `tbl_buyer_user` (buyer login), and no separate one for admin/employee. This is a pre-existing condition to account for, not something introduced by this analysis.
