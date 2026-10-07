# 04 — Migration Strategy & Ground Rules

How to move from the single database in `01-table-inventory.md` to the per-service ownership in `02-domain-ownership.md`, without a big-bang cutover.

## Sequencing

1. **Confirm price/data snapshotting in Order tables** (see `03-cross-service-dependencies.md`) — this must be true before Order can be split out at all, split first or not.
2. **Extract Reference Data first** — smallest domain, read-only, lowest risk, and every other service depends on the caching decision being settled before their own extraction.
3. **Extract Identity** — Seller and Buyer both depend on it for auth checks; extracting it early lets both later splits call a real service instead of a stub.
4. **Extract Buyer and Seller** (either order) — each is self-contained once Identity and Reference Data are external services.
5. **Extract Product last before Order** — highest table count and the most master data, but few external dependencies except Seller (`seller_id` validation).
6. **Extract Order last** — it depends on all four other services (Buyer, Seller, Product, Identity indirectly), so it can only move cleanly once they are already independent.

## Per-domain migration steps (repeat for each service)

1. Stand up the new service's database with the owned tables from `02-domain-ownership.md`.
2. Backfill via a one-time data copy (row counts must be pulled from Postgres first — not in code).
3. Replace in-process repository calls that crossed the future boundary with API calls, using the mapping in `03-cross-service-dependencies.md`.
4. Run the monolith and the new service side by side, with the monolith's table marked read-only, until the API-based path is verified.
5. Cut over writes to the new service; remove the table from the monolith schema.

## Ground rules

1. One service owns writes to any given table — no exceptions.
2. No cross-service database connections; all cross-domain reads/writes go through an API or event.
3. Reference/master data is cached locally in each consumer, not queried per request.
4. Any table with a business-history requirement (e.g. `tbl_order_item` pricing, `tbl_seller_history`) must store a snapshot of the data it depends on, not just a foreign key, so history doesn't change retroactively when the source service's data changes.
5. Don't extract a service until its cross-service dependencies (per `03-cross-service-dependencies.md`) are resolved — extracting Order before Buyer/Seller/Product exist independently just recreates the coupling across a network call instead of removing it.
6. Every open decision (e.g. Reference Data: shared service vs. cached copies) must be recorded before implementation starts, not decided ad hoc during the build.

## Open decisions to close before implementation

- Reference Data: dedicated service vs. cached local copies per consumer (see `02-domain-ownership.md`) — recommendation given, needs sign-off.
- `employees` table: confirm business owner and whether it belongs in this migration at all.
- `tbl_legal_content`: confirm it's static enough to sit inside Identity Service, or deserves its own tiny Content service.
- Row counts and actual query patterns (via `pg_stat_statements` or slow-query logs) should be pulled from the running Postgres instance to validate the sequencing above — this analysis is based on code/schema only, not production usage data.
