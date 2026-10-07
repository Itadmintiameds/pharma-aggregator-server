-- Migration: relax NOT NULL constraints to support seller registration
-- "save draft" flow (TempSellerStatus.DRAFT). Drafts are created/updated with
-- a partial TempSellerDraftRequestDTO where every field is optional, so the
-- columns that used to be mandatory at the DB level for a fully-submitted
-- registration must now tolerate NULLs until the draft is finalized via
-- POST /temp-sellers/draft/{tempSellerId}/finalize (which runs the same full
-- validation createTempSeller always has, before flipping status to OPEN).
--
-- Run this manually against EVERY environment, including dev/test on
-- ddl-auto=update. Hibernate's ddl-auto=update only ADDS missing
-- columns/tables — it never relaxes an existing column's NOT NULL
-- constraint, so this script is required everywhere, not just prod.
--
-- Safe to run multiple times: DROP NOT NULL is a no-op if the column is
-- already nullable.

-- ── 1. tbl_temp_seller ───────────────────────────────────────────────────────
ALTER TABLE tbl_temp_seller ALTER COLUMN seller_name DROP NOT NULL;
ALTER TABLE tbl_temp_seller ALTER COLUMN phone DROP NOT NULL;
ALTER TABLE tbl_temp_seller ALTER COLUMN email DROP NOT NULL;
ALTER TABLE tbl_temp_seller ALTER COLUMN gst_number DROP NOT NULL;
ALTER TABLE tbl_temp_seller ALTER COLUMN gst_document_file_url DROP NOT NULL;
ALTER TABLE tbl_temp_seller ALTER COLUMN company_registration_certificate_url DROP NOT NULL;
ALTER TABLE tbl_temp_seller ALTER COLUMN terms_accepted DROP NOT NULL;
ALTER TABLE tbl_temp_seller ALTER COLUMN company_type_id DROP NOT NULL;
ALTER TABLE tbl_temp_seller ALTER COLUMN seller_type_id DROP NOT NULL;

-- ── 2. tbl_temp_seller_address ───────────────────────────────────────────────
ALTER TABLE tbl_temp_seller_address ALTER COLUMN state_id DROP NOT NULL;
ALTER TABLE tbl_temp_seller_address ALTER COLUMN district_id DROP NOT NULL;
ALTER TABLE tbl_temp_seller_address ALTER COLUMN taluka_id DROP NOT NULL;
ALTER TABLE tbl_temp_seller_address ALTER COLUMN city DROP NOT NULL;
ALTER TABLE tbl_temp_seller_address ALTER COLUMN street DROP NOT NULL;
ALTER TABLE tbl_temp_seller_address ALTER COLUMN building_no DROP NOT NULL;
ALTER TABLE tbl_temp_seller_address ALTER COLUMN pin_code DROP NOT NULL;

-- ── 3. tbl_temp_seller_coordinator ───────────────────────────────────────────
ALTER TABLE tbl_temp_seller_coordinator ALTER COLUMN name DROP NOT NULL;
ALTER TABLE tbl_temp_seller_coordinator ALTER COLUMN designation DROP NOT NULL;
ALTER TABLE tbl_temp_seller_coordinator ALTER COLUMN email DROP NOT NULL;
ALTER TABLE tbl_temp_seller_coordinator ALTER COLUMN is_email_verified DROP NOT NULL;
ALTER TABLE tbl_temp_seller_coordinator ALTER COLUMN mobile DROP NOT NULL;
ALTER TABLE tbl_temp_seller_coordinator ALTER COLUMN is_phone_verified DROP NOT NULL;
ALTER TABLE tbl_temp_seller_coordinator ALTER COLUMN authorization_letter_url DROP NOT NULL;

-- ── 4. tbl_temp_seller_bank_details ──────────────────────────────────────────
ALTER TABLE tbl_temp_seller_bank_details ALTER COLUMN bank_name DROP NOT NULL;
ALTER TABLE tbl_temp_seller_bank_details ALTER COLUMN branch DROP NOT NULL;
ALTER TABLE tbl_temp_seller_bank_details ALTER COLUMN ifsc_code DROP NOT NULL;
ALTER TABLE tbl_temp_seller_bank_details ALTER COLUMN account_number DROP NOT NULL;
ALTER TABLE tbl_temp_seller_bank_details ALTER COLUMN account_holder_name DROP NOT NULL;
ALTER TABLE tbl_temp_seller_bank_details ALTER COLUMN bank_document_file_url DROP NOT NULL;
