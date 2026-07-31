-- Seed data: valid GST% slabs for product creation (GstPercentageMaster).
-- Run AFTER Hibernate ddl-auto=update has created tm_gst_percentage_master in dev/test,
-- or after the corresponding DDL has been applied in prod.
--
-- Idempotent: uses ON CONFLICT DO NOTHING against the primary key.

INSERT INTO tm_gst_percentage_master (gst_percentage_id, gst_percentage_value, is_active)
VALUES (1, 0, TRUE),
       (2, 5, TRUE),
       (3, 8, TRUE),
       (4, 10, TRUE),
       (5, 12, TRUE)
ON CONFLICT (gst_percentage_id) DO NOTHING;
