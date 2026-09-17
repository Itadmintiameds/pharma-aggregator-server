# Migration Roadmap: Monolith → 5-Service Architecture

Phased plan grounded in the coupling and boundaries established in `microservice-boundaries.md`, `database-ownership.md`, `event-architecture.md`, and `resilience-architecture.md`.

## Phase 0 — Harden the monolith first (prerequisite, not optional)

These are cheap relative to a full service extraction and reduce risk for everything after:

1. Fix the authorization gap in `SecurityConfig` (`anyRequest().permitAll()` → real rules) — see `security-architecture.md`. Doing this before extraction means each new service inherits a working auth model instead of copying a broken one five times.
2. Externalize `app.jwt.secret` from `application-dev.yml`.
3. Add idempotency keys to `OrderPlacementServiceImpl` and the seller/buyer approval endpoints (`resilience-architecture.md`).
4. Add a global `@RestControllerAdvice` for consistent error shapes.
5. Add explicit timeouts/basic retry to `EmailService`, `TwilioOTPService`, `S3Service`, `PdfService` calls.
6. Introduce a cache (Redis/Caffeine) in front of Master/Reference lookups — this is also exactly the mechanism the post-split Master/Reference module will need, so building it now is not wasted work.
7. Move Excel/CSV bulk import off the request thread to a background job with status polling.

## Phase 1 — Recommended first service to extract: **Master/Reference data**

### Recommended first extraction: Master/Reference (as a shared, cacheable module/service)

Rationale: it has the shallowest coupling (8 flat tables, no writes from outside the master controllers, no complex FK web pointing *into* it that would create a rollback dependency), it's consumed identically by Seller, TempSeller, Buyer, TempBuyer, and Product, and — critically — because Phase 0 already added caching in front of it, the actual data-access pattern for every consumer doesn't need to change on day one of the split (they keep reading from a local cache; only the cache's origin moves from "same DB" to "Master/Reference API"). This makes it the lowest-risk practice run for the extraction machinery (build pipeline, service-to-service auth, deployment) before attempting a domain with real write coupling.

Steps:
1. Stand up Master/Reference as its own Spring Boot service (or, if the team judges a full network service is overkill for 8 static tables, ship it as a versioned shared library + its own schema migration ownership — a legitimate alternative given the low write volume, see `microservice-boundaries.md`).
2. Point its schema at the same physical Postgres instance initially (schema-level separation, not yet a fully separate database) to de-risk the cutover — migrate to a physically separate database once the API contract is proven stable.
3. Update Seller, TempSeller, Buyer, TempBuyer, Product consumers to call the new API (through the Phase-0 cache layer) instead of their local `*MasterRepository` beans.
4. Decommission the in-process repositories for these 8 tables from the monolith once all consumers are migrated.

## Phase 2 — Extract Identity & Access

Rationale for going second: consolidates the three parallel auth stacks identified in `security-architecture.md`/`domain-analysis.md` into one place *while* extracting, turning a security cleanup and a service extraction into one project instead of two. Every other service will depend on it for token validation, so it needs to be stable and independently deployable before Seller/Buyer/Order/Product are pulled apart.

Steps:
1. Design one unified signup/OTP/login/JWT API replacing the three stacks (`entity/auth`, `seller/SellerLogIn`, `buyer` auth).
2. Migrate `tbl_user`, `tbl_signup_otp`, `tbl_login_otp`, `tbl_refresh_tokens`, `tbl_buyer_user`, `tbl_buyer_login_otp`, `tbl_buyer_refresh_tokens`, `tbl_buyer_signup_otp` to the new service's schema.
3. Update `AuthTokenFilter`/`JwtUtils` usage across the monolith to validate tokens against the new service (JWKS-style public-key validation preferred over a synchronous call-per-request, to avoid a new latency/availability dependency on every request).
4. Cut over Seller and Buyer registration/login flows to call the new service.

## Phase 3 — Extract Seller & Onboarding, and Buyer & Onboarding (in either order, or together)

These can proceed in parallel once Identity & Access and Master/Reference exist, since they depend on both but not on each other (their only shared touchpoint is Master/Reference, already extracted).

Steps per domain (Seller shown; Buyer mirrors it):
1. Stand up the service owning `tbl_seller*`, `tbl_temp_seller*`, `tbl_pending_seller*`, `tbl_seller_history`, `phone_otp`, `tbl_terms_master`.
2. Keep the `TempSeller → Seller` promotion (`SellerApprovalServiceImpl`) as a single local transaction inside this one new service — per `microservice-boundaries.md`, do not split temp-seller and seller further.
3. Replace direct `UserRepository`/Master-Reference repository injections with API calls to the already-extracted Identity and Master/Reference services.
4. Cut the frontend's seller registration wizard and admin approval screens over to the new service's API (coordinate with the `pharma-aggregator-client` frontend team, since its `CLAUDE.md` shows registration/approval flows are actively used).

## Phase 4 — Extract Product Catalog

Largest domain by entity count (~50 tables); do this after Seller exists so `ProductDetails.seller_id`/`StockLedger.seller_id` validation has a real API to call.

Steps:
1. Migrate the product/attribute/master/pricing/stock/packaging tables.
2. Replace the in-process `seller_id` FK with the API-validation pattern from `database-ownership.md`.
3. Move bulk import (already isolated behind `ProductImportStrategyFactory`) wholesale — its internal structure needs no redesign, just a new home.
4. Implement the stock-restore-on-cancel event consumer (from `event-architecture.md`) so Order's cancellation/return flow can be updated in the next phase without a stock-consistency gap.

## Phase 5 — Extract Order, Payment & Fulfillment (and fold in Quote)

Do this last: it is the domain most dependent on the other four (Buyer, Product/Stock, Seller all being called from `OrderPlacementServiceImpl`/`ReturnRefundServiceImpl` today), and per `resilience-architecture.md` it requires the order-placement saga/compensation design to be built and tested *before* cutover, not during.

Steps:
1. Design and test the order-placement saga (reserve stock via Product Catalog API → create order → confirm/compensate) in a staging environment against the already-extracted Buyer, Product, and Seller services.
2. Migrate `tbl_order*`, `tbl_payment`, `tbl_refund`, `tbl_return_request`, `tbl_invoice`, `tbl_quote_request`.
3. Cut the frontend order-placement/tracking flows over once the saga is validated under failure-injection testing (downstream service down, timeout, partial failure).

## Phase 6 — Decommission the monolith

Once all 5 services are live and the frontend is fully cut over, retire the original deployable, keeping only the shared Master/Reference module (or lightweight service) as the sole remaining piece of "shared" infrastructure by design.

## Cross-cutting notes

- Every phase assumes Phase 0's hardening is already in place — do not extract services out of a monolith with an open authorization gap; that gap gets copied five times.
- ASSUMPTION: this roadmap assumes incremental extraction with the strangler-fig pattern (old and new code paths coexisting behind a router/gateway during each phase) rather than a big-bang cutover, since no evidence of an existing API gateway or feature-flagging infrastructure was found in the repo — that infrastructure itself is a Phase 0/1 prerequisite not yet listed above and should be scoped with the team before Phase 1 begins.
- ASSUMPTION: team size and velocity are not known from the codebase; the 6-phase breakdown above assumes a small team executing sequentially — a larger org could parallelize Phases 3 and 4.
