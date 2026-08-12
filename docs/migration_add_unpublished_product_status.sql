-- Migration: add UNPUBLISHED as a valid product status.
--
-- Hibernate's ddl-auto=update does not alter existing CHECK constraints, so
-- this must be applied manually on every environment (dev/test included,
-- despite ddl-auto=update there) whenever the ProductStatus enum gains a
-- new constant.
--
-- Safe to run multiple times.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'tm_product_details_status_check'
    ) THEN
        ALTER TABLE tm_product_details DROP CONSTRAINT tm_product_details_status_check;
    END IF;

    ALTER TABLE tm_product_details
        ADD CONSTRAINT tm_product_details_status_check
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'UNPUBLISHED'));
END $$;
