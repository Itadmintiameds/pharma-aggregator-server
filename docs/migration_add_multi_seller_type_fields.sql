-- Migration: support White Labeling/Marketer, Distributor, PCD seller onboarding
-- (Manufacturer was the only fully onboarded seller type before this change)
--
-- Run this manually against environments where ddl-auto=validate (e.g. prod),
-- since Hibernate will not auto-create these there. Dev/test use ddl-auto=update
-- and Hibernate will create the same columns/table automatically on next boot,
-- so this script is optional (but harmless/idempotent) there.
--
-- Safe to run multiple times: every statement is guarded with IF NOT EXISTS
-- or an equivalent existence check.

-- ── 1. tbl_seller: Parent Manufacturer Name / Brand Owner Name ─────────────
ALTER TABLE tbl_seller
    ADD COLUMN IF NOT EXISTS parent_manufacturer_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS brand_owner_name VARCHAR(100);

ALTER TABLE tbl_temp_seller
    ADD COLUMN IF NOT EXISTS parent_manufacturer_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS brand_owner_name VARCHAR(100);

-- ── 2. Coordinator: Authorization Letter ───────────────────────────────────
ALTER TABLE tbl_seller_coordinator
    ADD COLUMN IF NOT EXISTS authorization_letter_url VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS is_authorization_letter_verified BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE tbl_temp_seller_coordinator
    ADD COLUMN IF NOT EXISTS authorization_letter_url VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS is_authorization_letter_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- ── 3. Bank Details: State / District / Taluka ─────────────────────────────
ALTER TABLE tbl_seller_bank_details
    ADD COLUMN IF NOT EXISTS state_id BIGINT REFERENCES tbl_state_master (state_id),
    ADD COLUMN IF NOT EXISTS district_id BIGINT REFERENCES tbl_district_master (district_id),
    ADD COLUMN IF NOT EXISTS taluka_id BIGINT REFERENCES tbl_taluka_master (taluka_id);

ALTER TABLE tbl_temp_seller_bank_details
    ADD COLUMN IF NOT EXISTS state_id BIGINT REFERENCES tbl_state_master (state_id),
    ADD COLUMN IF NOT EXISTS district_id BIGINT REFERENCES tbl_district_master (district_id),
    ADD COLUMN IF NOT EXISTS taluka_id BIGINT REFERENCES tbl_taluka_master (taluka_id);

-- ── 4. New lookup table: tbl_document_type_master ──────────────────────────
CREATE TABLE IF NOT EXISTS tbl_document_type_master
(
    document_type_id   BIGSERIAL PRIMARY KEY,
    document_type_name VARCHAR(150) NOT NULL UNIQUE,
    document_type_code VARCHAR(100) NOT NULL UNIQUE,
    is_active          BOOLEAN DEFAULT TRUE,
    created_by         VARCHAR(100),
    updated_by         VARCHAR(100),
    created_at         TIMESTAMP NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP
);

-- ── 5. SellerDocument / TempSellerDocument: documentType FK ────────────────
-- NOTE: product_type_id deliberately KEEPS its original NOT NULL constraint —
-- we do NOT relax it. Seller-level documents (agreements/certificates) that
-- have no real product category instead get pointed at a reserved placeholder
-- ProductTypeMaster row (see seed_seller_types_and_document_types.sql), so no
-- existing constraint on this shared, live table needs to change.
ALTER TABLE tbl_seller_document
    ADD COLUMN IF NOT EXISTS document_type_id BIGINT REFERENCES tbl_document_type_master (document_type_id);

ALTER TABLE tbl_temp_seller_document
    ADD COLUMN IF NOT EXISTS document_type_id BIGINT REFERENCES tbl_document_type_master (document_type_id);
