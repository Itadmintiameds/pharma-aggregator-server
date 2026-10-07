# Security Architecture

All claims below are traced to `config/SecurityConfig.java`, `config/CrossConfig.java`, `application*.yml`, and controller-level annotations found in `controller/**`.

## Authentication

- JWT-based, hand-rolled with `jjwt-api`/`jjwt-impl`/`jjwt-jackson` 0.11.5 (`security/JwtUtils`, referenced by `SecurityConfig`) — no Spring Authorization Server, no OAuth2 resource-server starter.
- `SecurityConfig` registers `AuthTokenFilter` (custom, wraps `JwtUtils` + `UserDetailsServiceImpl`) **before** `UsernamePasswordAuthenticationFilter`, and sets `SessionCreationPolicy.STATELESS` — confirms the app is designed to be stateless/token-based, consistent with the frontend `CLAUDE.md`'s description of `accessToken`/`refreshToken` stored in `localStorage`.
- **Three parallel credential/OTP stacks** exist at the entity level (see `domain-analysis.md`): `entity/auth` (`User`, `tbl_signup_otp`, `tbl_login_otp`, `tbl_refresh_tokens`) for seller identity, a second seller-specific stack under `seller/SellerLogIn` (`SellerUserRepository`, `refreshTokenRepository`), and a fully separate buyer stack (`BuyerUser`, `tbl_buyer_login_otp`, `tbl_buyer_refresh_tokens`, `tbl_buyer_signup_otp`). Each implements its own OTP-issue/verify/JWT-issue logic — this triples the attack surface for auth bugs and is the security-relevant counterpart to the coupling problem already flagged in `microservice-boundaries.md`'s Identity & Access proposal.
- JWT secret in `application-dev.yml` is a **plaintext literal in source control**: `app.jwt.secret: mySecretKeyForJWTTokenGenerationAndValidation2024!@#$%^&*` — not pulled from an environment variable or secrets manager in that profile (contrast with AWS/Twilio/mail credentials in the same file, which *are* externalized via `${...}` placeholders). Access-token expiration is also documented in the file as "temporarily 24 hours for testing" via a comment (`app.jwt.expiration: 86400000`), which is a full day rather than the intended 30 minutes noted in the adjacent comment — a live TODO left in a config file that ships with the repo.
- Password encoding uses `BCryptPasswordEncoder` (`SecurityConfig` bean) — a reasonable, standard choice.

## Authorization — effectively disabled

`SecurityConfig.filterChain()`:
```java
.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
```
The entire, more granular rule set (Swagger public, `/api/auth/**` public, `/api/public/**` public, "all other requests require authentication") is present in the file **but commented out**. As written, **every endpoint in the application — all 59 controllers, ~286 endpoint methods — permits anonymous access at the Spring Security filter-chain level**, regardless of the `Bearer` token. The `AuthTokenFilter` still runs and populates the `SecurityContext` when a valid token is present, so `@PreAuthorize`/method-level checks (if any) could still function, but no `@EnableMethodSecurity`-driven role restriction was found actually applied on any controller method during this pass (no `@PreAuthorize`/`@Secured`/`@RolesAllowed` annotations were observed on the controllers read). `@EnableMethodSecurity` is enabled in `SecurityConfig`, but the codebase does not appear to be exercising it on the endpoints inspected — meaning role differentiation between seller/buyer/admin is currently enforced (if at all) only inside individual controller/service method bodies via manual checks, not declaratively.

**This is the single highest-severity finding in this analysis**: the API is effectively open. Anyone who can reach the network path (which per CORS config below is broader than just the app's own frontends) can call any admin, seller, buyer, or order endpoint without a valid token. This should be treated as a production blocker, not an architectural nice-to-have, independent of any microservice migration.

## CORS

`CrossConfig.corsFilter()` — a global `CorsFilter` bean, applied to `/**`:
- `allowedOrigins`: `http://localhost:3000`, `https://pharma-aggregator-test.tiameds.ai`, `https://tiameds-admin-dashboard.vercel.app`, `https://admin-test.tiameds.ai`, `https://marketplace-frontend.loca.lt`
- `allowedMethods`: GET, POST, PUT, DELETE, OPTIONS, PATCH
- `allowedHeaders`: `*` (wildcard)
- `allowCredentials: true`

Note: `https://marketplace-frontend.loca.lt` is a `localtunnel` dev-tunnel domain checked into a config file alongside production origins (`tiameds.ai`, `vercel.app`) — a developer's temporary tunnel URL left in shared config is worth cleaning up, and combined with `allowCredentials: true` + wildcard headers, broadens the credentialed cross-origin surface further than production origins alone would need.

## CSRF

Explicitly disabled (`http.csrf(AbstractHttpConfigurer::disable)`) — acceptable for a stateless, token-based, cross-origin API (no cookie-based session auth to protect), consistent with the frontend's non-httpOnly `token` cookie being informational only per the frontend `CLAUDE.md`, not used for session auth.

## Role-based access observed in code

`entity/auth/User.java` has a `@ManyToMany(fetch = FetchType.EAGER)` to `tbl_role_master` — roles exist as a data model. `RoleMasterRepository` exists. However, with the authorization chain fully permissive (see above) and no `@PreAuthorize` found on controllers during this pass, role assignment currently has no enforcement point at the HTTP layer that was located in this analysis — it may be checked manually in some service method, but that was not confirmed for any endpoint read.

## Other security-relevant observations

- `application-prod.yml` datasource password is the literal `root` with no indication of secrets-manager injection (contrast with `dev`'s externalized AWS/mail/Twilio secrets) — if this file is what's actually deployed rather than overridden at runtime by env vars, this is a critical credential-hygiene issue. ASSUMPTION: production likely overrides this via environment/secret injection at deploy time and the committed value is a placeholder, but the repo alone does not prove that.
- `server.error.include-stacktrace: always` / `include-exception: true` in `application-dev.yml` — verbose error responses; confirm this is not also active in prod (it is not set in `application-prod.yml`, which is good, but the base `application.yml` doesn't set it either, so the effective value depends on Spring Boot's default, which for `include-stacktrace` defaults to `never` — likely fine).
- Multipart limits (5MB/file, 25MB/request, `max-part-count: 50`) provide some upload-DoS mitigation for the document/image upload endpoints (S3-backed seller/buyer document uploads, product images).
- Twilio and AWS credentials are read from environment variables (`${ACCOUNT_SID}`, `${AUTH_TOKEN}`, `${SERVICE_SID}`, `${AWS_ACCESS_KEY}`, `${AWS_SECRET_KEY}`, `${AWS_REGION}`, `${AWS_BUCKET_NAME}`) — correctly externalized, unlike the JWT secret.

## Recommendations (independent of any microservice work)

1. Re-enable the commented-out `authorizeHttpRequests` rule set in `SecurityConfig` immediately — restrict to genuinely public paths (Swagger, signup/login/OTP endpoints, public master-data lookups) and require authentication elsewhere.
2. Move `app.jwt.secret` to an environment variable/secrets manager in every profile, including dev.
3. Add `@PreAuthorize`/role checks on admin-only endpoints (`controller/admin/**`, `AdminSellerApprovalController`) given `@EnableMethodSecurity` is already turned on but unused.
4. Consolidate the three parallel auth stacks (see Identity & Access service in `microservice-boundaries.md`) — three independent OTP/JWT implementations means three places security fixes must be applied and kept in sync.
5. Remove the `loca.lt` tunnel origin from `CrossConfig` before any production hardening pass, and consider narrowing `allowedHeaders` from `*`.
