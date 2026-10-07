# Buyer Service — Low-Level Design (LLD)

Status: proposed (buyer-service repo currently a scaffold — no entities/controllers exist yet, see §7)
Companion doc to `microservice-boundaries.md` / `simple-hld.md` / `diagrams/best-architecture.drawio` / `diagrams/data-storage-view.drawio`. Follows the same fully-independent, no-shared-DB pattern already implemented in `pharma-aggregator-seller-service`, plus the Identity Index correction described in §6.1 (buyer-service and seller-service never call each other directly, even for the email-uniqueness check).

## 1. Total table count: 20

| Group | Count |
|---|---|
| Core Buyer (live, post-approval) | 9 |
| Temp/Staging Buyer (onboarding, pre-approval) | 5 |
| Private master/reference copies | 6 |
| **Total** | **20** |

All 20 tables are owned exclusively by buyer-service's own database (`buyerservice_db`, dedicated Postgres, mirroring seller-service's `sellerservice_db`). Zero tables are shared with seller-service, product-service, or read back from the monolith.

## 2. Table inventory

### 2.1 Core Buyer (9)

| # | Table | Purpose |
|---|---|---|
| 1 | `tbl_buyer` | Approved buyer organization record |
| 2 | `tbl_buyer_user` | Buyer-side auth identity (buyer's own user/login record — not the generic `tbl_user`) |
| 3 | `tbl_buyer_address` | Registered/billing address(es) |
| 4 | `tbl_buyer_contact` | Contact person(s) for a buyer org |
| 5 | `tbl_buyer_document` | KYC/verification documents (post-approval copies) |
| 6 | `tbl_buyer_delivery_address` | Delivery address book (distinct from billing address) |
| 7 | `tbl_buyer_login_otp` | OTP issued at login |
| 8 | `tbl_buyer_refresh_tokens` | JWT refresh token store |
| 9 | `tbl_buyer_signup_otp` | OTP issued at signup |

### 2.2 Temp/Staging Buyer (5)

| # | Table | Purpose |
|---|---|---|
| 10 | `tbl_temp_buyer` | Staging record during onboarding, pre-admin-approval |
| 11 | `tbl_temp_buyer_address` | Staging address |
| 12 | `tbl_temp_buyer_contact` | Staging contact person |
| 13 | `tbl_temp_buyer_document` | Uploaded documents awaiting review |
| 14 | `tbl_temp_buyer_review_history` | Admin review/audit trail before promotion |

### 2.3 Private master/reference copies (6)

| # | Table | Purpose |
|---|---|---|
| 15 | `tbl_state_master` | State lookup (own copy — no cross-service call) |
| 16 | `tbl_district_master` | District lookup |
| 17 | `tbl_taluka_master` | Taluka lookup |
| 18 | `tbl_buyer_type_master` | Buyer classification (retail/hospital/institutional, etc.) — moves here from seller-service, which currently holds a stray copy it doesn't own domain-wise |
| 19 | `tbl_document_type_master` | Document type lookup for KYC uploads |
| 20 | `tbl_terms_master` | T&C version buyer accepted at signup |

Admin approval adds **no new table** — it operates over `tbl_temp_buyer*` / `tbl_buyer*` in place, same as seller-service's `AdminSellerApprovalController` pattern.

## 3. Bounded context / scope

Buyer-service owns:
- Buyer self-registration and onboarding (staging → admin review → promotion to live buyer)
- Buyer-side authentication (signup OTP, login OTP, refresh tokens) — fully self-contained, no dependency on a shared Identity service
- Buyer profile management (addresses, delivery addresses, contacts, documents)
- Buyer-side admin approval workflow

Buyer-service does **not** own (out of scope, called by other services or not needed here):
- Orders, quotations, payments, invoices (owned by order-service/billing-service — buyer-service is a pure identity/profile service, not a transactional one)
- Product catalog/search (product-service/search-service)
- Notification delivery (notification-service) — buyer-service triggers/emits, doesn't send

## 4. Package structure (mirrors seller-service's flat layout)

```
com.tiameds.pharmaaggregator.buyerservice
├── config/          (security, CORS, S3, mail/SMS client config)
├── controller/      (BuyerController, TempBuyerController, AuthController,
│                      AuthenticationController, SignupController,
│                      BuyerDocumentController, AdminBuyerApprovalController)
├── dto/
├── entity/          (20 @Entity classes listed above)
├── exception/
├── mapper/
├── repository/      (20 Spring Data JPA repositories, 1:1 with entities)
├── response/
├── security/         (already exists — SecurityConfig.java)
├── service/
└── util/
```

## 5. Controllers (proposed, 1:1 with seller-service's precedent)

| Controller | Base path | Key endpoints |
|---|---|---|
| `AuthController` | `/auth` | `/reset-password`, `/forgot-password`, `/verify-otp`, `/validate-reset-token`, `/reset-password-with-token` |
| `AuthenticationController` | `/authentication` | `/login`, `/verify-otp`, `/refresh`, `/logout` |
| `SignupController` | `/auth/signup` | `POST /`, `/verify-otp` |
| `TempBuyerController` | `/temp-buyers` | CRUD, `GET /user/{userId}`, `PATCH /{id}/verify/document`, document upload/delete, `POST /draft`, `PUT /draft/{id}`, `POST /draft/{id}/finalize` |
| `BuyerProfileController` | `/buyers` | `PUT /{buyerId}/request-update`, `GET /user/{userId}`, `GET /`, `DELETE /{buyerId}`, delivery-address CRUD |
| `BuyerTypeMasterController` | `/buyer-types` | `GET /` |
| `AdminBuyerApprovalController` | `/admin/buyer-requests` | `GET /pending`, `/pending/{id}`, `/{buyerId}`, `POST /{id}/approve`, `/{id}/reject`, `/batch-approve` |

## 6. Cross-cutting design points

- **DB**: dedicated Postgres, `buyerservice_db`, `ddl-auto: update` (dev) / Flyway migrations under `db/migration` — same convention as seller-service and the monolith.
- **Auth**: JWT, self-issued and self-verified within buyer-service (own `tbl_buyer_refresh_tokens`), no call-out to seller-service or a shared Identity/Auth service.
- **Gateway route**: add one Spring Cloud Gateway route in `pharma-aggregator-api-gateway/application.yaml`, `id: buyer-service`, `uri: http://${BUYER_SERVICE_HOST:buyer-service}:${BUYER_SERVICE_PORT:8081}`, predicate `Path=/api/v1/**` scoped to buyer paths (or path-prefix split, e.g. `/api/v1/buyers/**`, `/api/v1/temp-buyers/**`), filter `StripPrefix=2`.
- **docker-compose**: add `buyer-service` + `buyer-postgres` to root `docker-compose.yml`, on the shared `platform` network, following the seller-service block exactly (env `BUYER_DB_NAME`, `BUYER_SERVICE_HOST`, `BUYER_SERVICE_PORT`).
- **No direct inter-service calls**: consistent with "separate everything" — buyer-service never calls seller-service or product-service directly, and vice versa. The one cross-cutting concern that used to imply a direct call (email uniqueness at signup) is solved without one — see §6.1.
- **Master data duplication accepted**: this doc treats the master-table duplication as a deliberate, accepted tradeoff (per your instruction to separate everything) rather than a defect. Revisit only if data-consistency issues surface across services' copies of state/district/taluka.

### 6.1 Email uniqueness at signup — Identity Index Service (corrected design)

An earlier draft of this plan had buyer-service calling seller-service's API directly during signup to check whether an email was already registered as a seller. **That was wrong** — it creates a live runtime dependency between two domains that are supposed to be fully independent (seller-service being slow/down would then break buyer signup, and vice versa). The corrected design:

- A small standalone **Identity Index Service** (`identity-index-service`, suggested AWS backing: **DynamoDB**, table `identity_email_index`, PK `email`, attribute `owning_service`) is the only thing either service talks to for this check. It is **not** an Auth service and holds no passwords/tokens — purely `email → owning_service`.
- **Buyer-service and seller-service never call each other.** Both only call the Identity Index.
- **Signup flow**: buyer-service queries the Identity Index synchronously ("is this email already registered?") before writing `tbl_temp_buyer`. If taken, reject with a clear error.
- **Index is kept up to date asynchronously**: buyer-service publishes a `BuyerRegistered{email, buyerId}` event to the shared event bus (suggested: **Amazon SNS + SQS**) after writing `tbl_temp_buyer`; seller-service publishes `SellerRegistered{email, sellerId}` the same way. A consumer updates the Identity Index off these events — buyer-service's own DB write is never blocked waiting for the index.
- **Failure mode — Identity Index unavailable**: the check is wrapped in a circuit breaker (Resilience4j: short timeout, e.g. 300ms) with a **fail-open fallback** — if the index doesn't respond in time, signup proceeds anyway (flagged `uniqueness_check_status = 'SKIPPED'` on the temp record) rather than blocking a legitimate customer. The event bus keeps buffering `BuyerRegistered`/`SellerRegistered` events regardless of the index's availability, so once it recovers it catches up from the backlog — no event is lost, only the check is briefly skipped. A background reconciliation job periodically scans for any true duplicate that slipped through during that window and flags it for admin resolution.
- **Not routed through the API Gateway** — the Identity Index is internal service-to-service traffic only (buyer-service/seller-service → index, and event bus → index), reachable via the internal network/service discovery, never exposed as a public route.
- Full picture: `diagrams/best-architecture.drawio` (service/communication view) and `diagrams/data-storage-view.drawio` (data-ownership view, explicitly marks the Identity Index as a fast projection, **not** the source of truth — `buyerservice_db`/`sellerservice_db` remain authoritative for their own users).

## 7. Current implementation status

`pharma-aggregator-buyer-service` today has **0 of 20 tables and 0 of 7 controllers implemented** — only `BuyerServiceApplication.java` and `security/SecurityConfig.java` exist, with empty package directories. This doc defines the target scope to build against.
