-- tbl_buyer_user.is_password_temporary (BuyerUser.java) was added as
-- nullable=false with no DB default. Hibernate ddl-auto=update cannot add a
-- NOT NULL column to a non-empty table without a default value — Postgres
-- rejects it, Hibernate only logs a warning instead of failing startup, so
-- the app boots but the column was never actually added. Every insert into
-- tbl_buyer_user has been failing since.
ALTER TABLE tbl_buyer_user
    ADD COLUMN IF NOT EXISTS is_password_temporary BOOLEAN NOT NULL DEFAULT FALSE;
