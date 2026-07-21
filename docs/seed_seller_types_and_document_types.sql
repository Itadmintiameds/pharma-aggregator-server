-- Seed data: new seller types + document types for multi-seller-type onboarding.
-- Run AFTER migration_add_multi_seller_type_fields.sql (or after Hibernate ddl-auto=update
-- has created tbl_document_type_master in dev/test).
--
-- Idempotent: uses ON CONFLICT DO NOTHING against the unique columns.

-- ── 1. New seller types (Manufacturer already exists) ──────────────────────
-- seller_type_abbreviation is used to build Seller IDs (see
-- SellerApprovalServiceImpl.generateSellerId), keep to <= 3 chars.
INSERT INTO tbl_seller_type_master (seller_type_name, seller_type_abbreviation, is_active, created_by, updated_by)
VALUES ('White Labeling Company / Marketer', 'WLM', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Distributor', 'DIS', TRUE, 'SYSTEM', 'SYSTEM'),
       ('PCD', 'PCD', TRUE, 'SYSTEM', 'SYSTEM')
ON CONFLICT (seller_type_name) DO NOTHING;

-- ── 2. Document types ───────────────────────────────────────────────────────
-- document_type_code values here match the constants checked in
-- SellerTypeFieldValidator (BRAND_OWNER_AGREEMENT, WHITE_LABELING_AGREEMENT,
-- DISTRIBUTION_AGREEMENT, PCD_AGREEMENT) — do not rename those 4 codes without
-- updating that class too.
INSERT INTO tbl_document_type_master (document_type_name, document_type_code, is_active, created_by, updated_by)
VALUES ('Drug Manufacturing Licence', 'DRUG_MANUFACTURING_LICENCE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Wholesale Drug Licence (20B/21B)', 'WHOLESALE_DRUG_LICENCE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('FSSAI Manufacturing Licence', 'FSSAI_MANUFACTURING_LICENCE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('FSSAI Trade / Marketing Licence', 'FSSAI_TRADE_MARKETING_LICENCE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Cosmetic Manufacturing Licence', 'COSMETIC_MANUFACTURING_LICENCE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Cosmetic Marketing Licence', 'COSMETIC_MARKETING_LICENCE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Cosmetic Wholesale Licence', 'COSMETIC_WHOLESALE_LICENCE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Medical Device Manufacturing Licence', 'MEDICAL_DEVICE_MANUFACTURING_LICENCE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Medical Device Wholesale Licence (MD-42)', 'MEDICAL_DEVICE_WHOLESALE_LICENCE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('GMP / WHO-GMP Certificate', 'GMP_WHO_GMP_CERTIFICATE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Brand Owner Agreement', 'BRAND_OWNER_AGREEMENT', TRUE, 'SYSTEM', 'SYSTEM'),
       ('White Labeling Agreement', 'WHITE_LABELING_AGREEMENT', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Distribution Agreement', 'DISTRIBUTION_AGREEMENT', TRUE, 'SYSTEM', 'SYSTEM'),
       ('PCD Agreement', 'PCD_AGREEMENT', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Trademark Certificate', 'TRADEMARK_CERTIFICATE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('IEC Certificate', 'IEC_CERTIFICATE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Drug Import Licence', 'DRUG_IMPORT_LICENCE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('FSSAI Import Licence', 'FSSAI_IMPORT_LICENCE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Cosmetic Import Licence', 'COSMETIC_IMPORT_LICENCE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Medical Device Import Licence', 'MEDICAL_DEVICE_IMPORT_LICENCE', TRUE, 'SYSTEM', 'SYSTEM')
ON CONFLICT (document_type_code) DO NOTHING;

-- ── 3. Placeholder product type for seller-level documents ─────────────────
-- tbl_seller_document.product_type_id / tbl_temp_seller_document.product_type_id
-- keep their original NOT NULL constraint (not relaxed). Seller-level
-- documents (agreements/certificates like Brand Owner Agreement, GMP
-- Certificate) have no real product category, so they point at this reserved,
-- INACTIVE row instead of leaving product_type_id null. is_active = FALSE
-- keeps it out of GET /product-types (see ProductTypeMasterServiceImpl's
-- isActive filter), so it never appears in the seller's category picker.
-- The exact name is matched by TempSellerServiceImpl.resolvePlaceholderProductType()
-- via findByProductTypeNameIgnoreCase — do not rename without updating that too.
INSERT INTO tbl_product_type_master (product_type_name, regulatory_category, is_active, created_by, updated_by)
VALUES ('N/A - Seller Level Document', 'N/A', FALSE, 'SYSTEM', 'SYSTEM')
ON CONFLICT (product_type_name) DO NOTHING;
