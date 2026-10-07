-- Flyway baseline migration: ensures tbl_document_type_master and
-- tbl_buyer_type_master exist, then seeds the buyer types + their mandatory
-- document types needed for buyer registration (see BuyerTypeMaster /
-- DocumentTypeMaster entities).
--
-- CREATE TABLE IF NOT EXISTS is used because these tables are normally
-- auto-created by Hibernate (ddl-auto=update) in dev/test — on those
-- environments this is a no-op and only the INSERT statements below apply.
-- On a fresh environment (schema not yet created by Hibernate) this
-- migration creates the tables itself so the seed data can be inserted
-- before the application finishes starting.

CREATE TABLE IF NOT EXISTS tbl_document_type_master (
    document_type_id   BIGSERIAL PRIMARY KEY,
    document_type_name VARCHAR(150) NOT NULL UNIQUE,
    document_type_code VARCHAR(100) NOT NULL UNIQUE,
    is_active           BOOLEAN DEFAULT TRUE,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tbl_buyer_type_master (
    buyer_type_id               BIGSERIAL PRIMARY KEY,
    buyer_type_name              VARCHAR(100) NOT NULL UNIQUE,
    buyer_type_abbreviation      VARCHAR(100) NOT NULL UNIQUE,
    mandatory_document_type_id  BIGINT REFERENCES tbl_document_type_master (document_type_id),
    is_active                    BOOLEAN DEFAULT TRUE,
    created_by                   VARCHAR(100),
    updated_by                   VARCHAR(100),
    created_at                   TIMESTAMP,
    updated_at                   TIMESTAMP
);

-- ── 1. Document types required by buyer registration ───────────────────────
INSERT INTO tbl_document_type_master (document_type_name, document_type_code, is_active, created_by, updated_by)
VALUES ('Drug License', 'DRUG_LICENSE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Clinical Establishment License', 'CLINICAL_ESTABLISHMENT_LICENSE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Laboratory Registration Certificate', 'LABORATORY_REGISTRATION_CERTIFICATE', TRUE, 'SYSTEM', 'SYSTEM'),
       ('Medical Council or Clinic Registration Certificate', 'MEDICAL_COUNCIL_CLINIC_REGISTRATION_CERTIFICATE', TRUE, 'SYSTEM', 'SYSTEM')
ON CONFLICT (document_type_code) DO NOTHING;

-- ── 2. Buyer types, each FK'd to its mandatory document type ───────────────
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
