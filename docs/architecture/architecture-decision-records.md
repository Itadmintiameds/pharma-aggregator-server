# Architecture Decision Records

## ADR-001: Adopt a 5-service target architecture, not one service per domain package

**Status:** Proposed
**Context:** `domain-analysis.md` identifies ~13 business-domain packages in the current monolith (auth, seller, temp-seller, buyer, temp-buyer, admin, product, master, order, quote, content, ifsc, employee). A naive reading might suggest one microservice per package.
**Decision:** Group into 5 coarse-grained services (Identity & Access; Seller & Onboarding; Buyer & Onboarding; Product Catalog; Order/Payment/Fulfillment) plus a shared Master/Reference module, per `microservice-boundaries.md`.
**Rationale:** `TempSellerServiceImpl` alone injects 11 repositories + 4 services; `SellerApprovalServiceImpl` transactionally bridges TempSeller → Seller in one local transaction; the product attribute tables are already internally decomposed by the Strategy pattern. Splitting along package boundaries would turn several single-transaction, tightly-cohesive workflows into distributed transactions with no offsetting benefit.
**Consequences:** Fewer independent deployment units to operate (good for a small team, per the scalability doc's team-size assumption), but each of the 5 services is larger and will itself need internal modularity discipline to avoid becoming its own mini-monolith.

## ADR-002: Keep Master/Reference data as a shared, cached module rather than a chatty microservice

**Status:** Proposed
**Context:** `tbl_state_master`/`tbl_district_master`/`tbl_taluka_master`/`tbl_company_type_master`/`tbl_seller_type_master`/`tbl_buyer_type_master`/`tbl_document_type_master`/`tbl_product_type_master` are read by nearly every other domain and written to rarely.
**Decision:** Do not stand up a fully independent, synchronously-called microservice for these 8 tables as the primary access pattern. Serve them through per-consumer caching (Redis/Caffeine) with either a lightweight owning service or a shared library, refreshed on a schedule or via a low-volume change event.
**Rationale:** These tables' read:write ratio is extremely high and their data changes infrequently (states/districts don't change; company/seller/buyer types are enumerations). A synchronous network call on every form load (seller/buyer address entry, product master lookups) for data this static would add latency and a new failure mode for no consistency benefit.
**Consequences:** Slight staleness window on master-data updates (acceptable given how rarely these change); consuming services must implement/maintain a cache, which is extra code but was already recommended independently in `scalability-architecture.md`.

## ADR-003: Extract Master/Reference first, not Identity & Access

**Status:** Proposed
**Context:** Multiple candidate "first extractions" exist; Identity & Access is often the textbook first choice because everything depends on auth.
**Decision:** Extract Master/Reference first (Phase 1 in `migration-roadmap.md`), Identity & Access second.
**Rationale:** Master/Reference has the shallowest coupling profile (no writes originating outside its own 8 master controllers, no inbound FK dependency that would create a rollback risk) and, because Phase 0 already introduces caching in front of it, consumers' code barely changes on cutover — it is the lowest-risk way to prove out the extraction pipeline (build, deploy, service-to-service auth) before attempting a domain with real write coupling and security consequences like Identity & Access.
**Consequences:** Identity & Access — despite being logically foundational — is deliberately extracted second, meaning the three-parallel-auth-stack consolidation (a real security debt item) is delayed by one phase relative to a "security first" instinct. This is judged acceptable because Phase 0 already remediates the most severe standalone security issue (`anyRequest().permitAll()`) before any extraction begins.

## ADR-004: Keep TempSeller/PendingSeller/Seller as one service, not three

**Status:** Proposed
**Context:** `entity/seller`, `entity/seller/profile` (PendingSeller), and `entity/temp/seller` represent three stages of the same seller-onboarding lifecycle, each with its own table family.
**Decision:** Own all three inside a single "Seller & Onboarding" service (see ADR-001 and `microservice-boundaries.md`).
**Rationale:** `SellerApprovalServiceImpl` promotes a `TempSeller` to a `Seller` in one local `@Transactional` today, reading `TempSellerRepository`/`TempSellerReviewHistoryRepository` and writing `SellerRepository`. There is no independent scaling or ownership reason found in the code to split these — they are one workflow with three states, not three domains.
**Consequences:** The Seller & Onboarding service will be one of the larger of the 5 (owns ~20 tables) — accepted as the correct tradeoff versus introducing a saga for what is currently a simple, safe local transaction.

## ADR-005: Treat the current authorization gap as a Phase 0 blocker, independent of the migration timeline

**Status:** Accepted (as a recommendation)
**Context:** `SecurityConfig.filterChain()` currently applies `anyRequest().permitAll()` to all 59 controllers / ~286 endpoints; the intended role-based rule set exists in the file but is commented out.
**Decision:** Fix this in Phase 0, before any service extraction begins, not as part of the Identity & Access extraction in Phase 2.
**Rationale:** Extracting services out of a monolith with an open authorization gap would copy the gap into (at minimum) the new service's edge, and likely delay the fix further behind migration priorities. Since the fix is a small, self-contained change to one file, there's no reason to couple it to the larger migration timeline.
**Consequences:** None significant — this is a low-cost, high-value fix that should not be blocked on architectural decisions elsewhere in this document.

## ADR-006: Introduce async/event patterns only where the codebase's coupling actually forces them, not speculatively

**Status:** Proposed
**Context:** No messaging infrastructure exists today (`event-architecture.md`). A full event-driven redesign could be applied everywhere.
**Decision:** Limit initial async/event adoption to (a) the order-placement saga's compensation path, (b) stock-restore-on-cancel/return, and (c) approval-notification decoupling — the three places where a genuine cross-service transactional or latency problem was identified in the code. Master/Reference propagation uses simple caching, not an event bus (ADR-002).
**Rationale:** Given no existing messaging dependency and an inferred small team (`scalability-architecture.md`), introducing a full event-driven architecture (e.g. Kafka with dozens of topics) for a system with 5 target services would be disproportionate. Scope the investment to where the code's actual coupling (found via `api-dependency-analysis.md`) demands it.
**Consequences:** Some read-model staleness (e.g. seller name shown on a product listing) may need to be addressed with a follow-up denormalization event later if it becomes a measured problem — deferred deliberately (see `event-architecture.md` item 4), not overlooked.

## ADR-007: Consolidate the three parallel authentication stacks during the Identity & Access extraction

**Status:** Proposed
**Context:** `entity/auth` (seller identity), `seller/SellerLogIn` (a second seller-specific auth stack), and buyer's own `BuyerUser`/OTP/refresh-token tables each independently implement signup-OTP → login-OTP → JWT issuance.
**Decision:** Do not preserve three implementations behind one service boundary — design one unified auth API during Phase 2 and migrate both seller stacks and the buyer stack onto it.
**Rationale:** Three independent implementations of the same security-sensitive logic (OTP generation/expiry, refresh-token rotation, JWT signing) triples the surface area for the same class of bug and triples future maintenance cost; the extraction is a natural forcing function to fix this rather than mechanically relocating the duplication.
**Consequences:** Phase 2 is larger in scope than a pure "lift and shift" would be, but avoids permanently baking the duplication into the target architecture.
