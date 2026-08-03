-- Seed data for tm_strength_unit.
--
-- This project has no Flyway/Liquibase (ddl-auto: update creates the table
-- structure only, not rows), and tm_category itself has no committed seed
-- script either — its data was inserted by hand. So this file is meant to
-- be run manually too, once tm_strength_unit exists (i.e. after the backend
-- has started at least once with the StrengthUnit entity present).
--
-- Verified against the actual test DB on 2026-08-03 — tm_category currently
-- has these rows:
--   1  Drugs
--   2  Supplements / Nutraceuticals
--   3  Food & Infant Nutrition
--   4  Cosmetic & Personal Use
--   5  Consumable Medical Devices & Equipment
--   6  Non-Consumable Medical Devices & Equipment
--
-- Matching is done with ILIKE rather than exact category_name strings, so
-- this still works if names get tweaked later. Each INSERT also skips any
-- (unit_name, category_id) pair that already exists, so it's safe to re-run.

-- Drugs
INSERT INTO tm_strength_unit (unit_name, category_id, is_active)
SELECT u.unit_name, c.category_id, true
FROM tm_category c
CROSS JOIN (VALUES
    ('mcg'), ('mg'), ('g'), ('IU'), ('Units'),
    ('mg/mL'), ('mcg/mL'), ('mg/5 mL'),
    ('%'), ('% w/w'), ('% w/v'), ('mEq'), ('mmol')
) AS u(unit_name)
WHERE c.category_name ILIKE '%Drug%'
  AND NOT EXISTS (
      SELECT 1 FROM tm_strength_unit su
      WHERE su.category_id = c.category_id
        AND lower(su.unit_name) = lower(u.unit_name)
  );

-- Supplements / Nutraceuticals
INSERT INTO tm_strength_unit (unit_name, category_id, is_active)
SELECT u.unit_name, c.category_id, true
FROM tm_category c
CROSS JOIN (VALUES
    ('mcg'), ('mg'), ('g'), ('IU'),
    ('Billion CFU'), ('Million CFU'), ('mg/mL')
) AS u(unit_name)
WHERE c.category_name ILIKE '%Supplement%'
  AND NOT EXISTS (
      SELECT 1 FROM tm_strength_unit su
      WHERE su.category_id = c.category_id
        AND lower(su.unit_name) = lower(u.unit_name)
  );

-- Food & Infant Nutrition
INSERT INTO tm_strength_unit (unit_name, category_id, is_active)
SELECT u.unit_name, c.category_id, true
FROM tm_category c
CROSS JOIN (VALUES
    ('g'), ('kg'), ('mg'), ('mcg'), ('IU'), ('kcal'), ('mL'), ('L'),
    ('g/100 g'), ('mg/100 g'), ('mg/100 mL')
) AS u(unit_name)
WHERE (c.category_name ILIKE '%Food%' OR c.category_name ILIKE '%Infant%')
  AND NOT EXISTS (
      SELECT 1 FROM tm_strength_unit su
      WHERE su.category_id = c.category_id
        AND lower(su.unit_name) = lower(u.unit_name)
  );

-- Cosmetic & Personal Use
INSERT INTO tm_strength_unit (unit_name, category_id, is_active)
SELECT u.unit_name, c.category_id, true
FROM tm_category c
CROSS JOIN (VALUES
    ('%'), ('% w/w'), ('% w/v'), ('mg/g'), ('mg/mL'), ('g'), ('mL')
) AS u(unit_name)
WHERE c.category_name ILIKE '%Cosmetic%'
  AND NOT EXISTS (
      SELECT 1 FROM tm_strength_unit su
      WHERE su.category_id = c.category_id
        AND lower(su.unit_name) = lower(u.unit_name)
  );

-- Consumable / Non-Consumable Medical Devices & Equipment intentionally get
-- no rows — strength units are "Not Applicable" for these categories per
-- the reference table.

-- Verify afterwards:
-- SELECT c.category_name, su.unit_name
-- FROM tm_strength_unit su
-- JOIN tm_category c ON c.category_id = su.category_id
-- ORDER BY c.category_name, su.unit_name;
