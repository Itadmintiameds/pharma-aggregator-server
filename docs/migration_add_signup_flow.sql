-- Migration: standalone "signup first" flow for seller registration
-- (email + password + OTP creates a login BEFORE any company/registration
-- details are filled in; the resulting user is then linked to the
-- registration they go on to create).
--
-- Run this manually against environments where ddl-auto=validate (e.g. prod),
-- since Hibernate will not auto-create these there. Dev/test use ddl-auto=update
-- and Hibernate will create the same columns/table automatically on next boot,
-- so this script is optional (but harmless/idempotent) there.
--
-- Safe to run multiple times: every statement is guarded with IF NOT EXISTS
-- or an equivalent existence check.

-- ── 1. tbl_signup_otp: OTP + hashed password held until email is verified ──
CREATE TABLE IF NOT EXISTS tbl_signup_otp (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255),
    otp           VARCHAR(255),
    password_hash VARCHAR(255),
    expiry_time   TIMESTAMP,
    verified      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP
);

-- ── 2. tbl_temp_seller: link to the User created at signup ─────────────────
ALTER TABLE tbl_temp_seller
    ADD COLUMN IF NOT EXISTS user_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_temp_seller_user'
    ) THEN
        ALTER TABLE tbl_temp_seller
            ADD CONSTRAINT fk_temp_seller_user FOREIGN KEY (user_id) REFERENCES tbl_user(user_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_temp_seller_user'
    ) THEN
        ALTER TABLE tbl_temp_seller
            ADD CONSTRAINT uk_temp_seller_user UNIQUE (user_id);
    END IF;
END $$;
