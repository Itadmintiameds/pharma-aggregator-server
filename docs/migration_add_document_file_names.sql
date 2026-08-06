-- Migration: add columns to persist the ORIGINAL filename of uploaded
-- documents (as the browser saw it, e.g. "Company_Cert_Scan_2024.pdf"),
-- alongside the existing *_url columns which only ever stored the S3
-- URL/key (from which only the file extension could be recovered).
--
-- Run this manually against EVERY environment, including dev/test on
-- ddl-auto=update. Hibernate's ddl-auto=update DOES add missing columns
-- automatically on startup, but this script documents the exact columns
-- added and lets you apply them ahead of a deploy without waiting on
-- application startup.
--
-- Safe to run multiple times: guard each statement with
-- IF NOT EXISTS where the target database supports it.

-- ── 1. tbl_temp_seller ───────────────────────────────────────────────────────
ALTER TABLE tbl_temp_seller ADD COLUMN IF NOT EXISTS company_registration_certificate_file_name VARCHAR(255);
ALTER TABLE tbl_temp_seller ADD COLUMN IF NOT EXISTS gst_document_file_name VARCHAR(255);

-- ── 2. tbl_temp_seller_coordinator ───────────────────────────────────────────
ALTER TABLE tbl_temp_seller_coordinator ADD COLUMN IF NOT EXISTS authorization_letter_file_name VARCHAR(255);

-- ── 3. tbl_temp_seller_bank_details ──────────────────────────────────────────
ALTER TABLE tbl_temp_seller_bank_details ADD COLUMN IF NOT EXISTS bank_document_file_name VARCHAR(255);

-- ── 4. tbl_temp_seller_document ──────────────────────────────────────────────
ALTER TABLE tbl_temp_seller_document ADD COLUMN IF NOT EXISTS document_file_name VARCHAR(255);
