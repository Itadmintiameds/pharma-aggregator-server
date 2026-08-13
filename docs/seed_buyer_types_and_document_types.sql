-- Seed data: buyer types + their mandatory document types for buyer onboarding.
-- Run AFTER Hibernate ddl-auto=update has created tbl_buyer_type_master /
-- tbl_document_type_master in dev/test.
--
-- Idempotent: uses ON CONFLICT DO NOTHING against the unique columns.

-- ── 1. Document types required by buyer registration ───────────────────────
-- GST Certificate / PAN Card codes are reused from the existing seller seed
-- (docs/seed_seller_types_and_document_types.sql) if already present; this
-- script only inserts the 4 new buyer-specific document types below.
INSERT INTO tbl_document_type_master (document_type_name, document_type_code, is_active, created_by, updated_by)
VALUES ('Drug License', 'DRUG_LICENSE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Clinical Establishment License', 'CLINICAL_ESTABLISHMENT_LICENSE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Laboratory Registration Certificate', 'LABORATORY_REGISTRATION_CERTIFICATE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Medical Council or Clinic Registration Certificate', 'MEDICAL_COUNCIL_CLINIC_REGISTRATION_CERTIFICATE', TRUE, 'SYSTEM', 'SYSTEM')
ON CONFLICT (document_type_code) DO NOTHING;

-- ── 2. Buyer types, each FK'd to its mandatory document type ───────────────
-- buyer_type_abbreviation mirrors seller_type_abbreviation's role (kept
-- short/uppercase) — used as part of the generated Buyer ID on approval
-- (see BuyerApprovalServiceImpl.generateBuyerId).
INSERT INTO tbl_buyer_type_master (buyer_type_name, buyer_type_abbreviation, mandatory_document_type_id, is_active, created_by, updated_by)
SELECT 'Hospital', 'HOS', dtm.document_type_id, TRUE, 'SYSTEM', 'SYSTEM'
FROM tbl_document_type_master dtm
WHERE dtm.document_type_code = 'CLINICAL_ESTABLISHMENT_LICENSE'
ON CONFLICT (buyer_type_name) DO NOTHING;

INSERT INTO tbl_buyer_type_master (buyer_type_name, buyer_type_abbreviation, mandatory_document_type_id, is_active, created_by, updated_by)
SELECT 'Clinic', 'CLN', dtm.document_type_id, TRUE, 'SYSTEM', 'SYSTEM'
FROM tbl_document_type_master dtm
WHERE dtm.document_type_code = 'MEDICAL_COUNCIL_CLINIC_REGISTRATION_CERTIFICATE'
ON CONFLICT (buyer_type_name) DO NOTHING;

INSERT INTO tbl_buyer_type_master (buyer_type_name, buyer_type_abbreviation, mandatory_document_type_id, is_active, created_by, updated_by)
SELECT 'Hospital Pharmacy', 'HPH', dtm.document_type_id, TRUE, 'SYSTEM', 'SYSTEM'
FROM tbl_document_type_master dtm
WHERE dtm.document_type_code = 'DRUG_LICENSE'
ON CONFLICT (buyer_type_name) DO NOTHING;

INSERT INTO tbl_buyer_type_master (buyer_type_name, buyer_type_abbreviation, mandatory_document_type_id, is_active, created_by, updated_by)
SELECT 'Pharmacy', 'PHM', dtm.document_type_id, TRUE, 'SYSTEM', 'SYSTEM'
FROM tbl_document_type_master dtm
WHERE dtm.document_type_code = 'DRUG_LICENSE'
ON CONFLICT (buyer_type_name) DO NOTHING;

INSERT INTO tbl_buyer_type_master (buyer_type_name, buyer_type_abbreviation, mandatory_document_type_id, is_active, created_by, updated_by)
SELECT 'Diagnostic Centre', 'DIA', dtm.document_type_id, TRUE, 'SYSTEM', 'SYSTEM'
FROM tbl_document_type_master dtm
WHERE dtm.document_type_code = 'LABORATORY_REGISTRATION_CERTIFICATE'
ON CONFLICT (buyer_type_name) DO NOTHING;

INSERT INTO tbl_buyer_type_master (buyer_type_name, buyer_type_abbreviation, mandatory_document_type_id, is_active, created_by, updated_by)
SELECT 'Laboratory', 'LAB', dtm.document_type_id, TRUE, 'SYSTEM', 'SYSTEM'
FROM tbl_document_type_master dtm
WHERE dtm.document_type_code = 'LABORATORY_REGISTRATION_CERTIFICATE'
ON CONFLICT (buyer_type_name) DO NOTHING;
