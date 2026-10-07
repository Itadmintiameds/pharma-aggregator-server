# Current Architecture — pharma-aggregator-server

## Summary

`pharma-aggregator-server` is a single-deployable Spring Boot monolith (Spring Boot 4.0.1, Java 17, Maven) backing a pharmacy B2B marketplace. It exposes a REST API under context path `/api/v1` and persists to a single PostgreSQL database via Spring Data JPA/Hibernate (`ddl-auto: update` in dev, `validate` in prod), with Flyway enabled (`classpath:db/migration`, baseline-on-migrate).

Base package: `com.example.pharmaaggregatorserver`.

## Tech stack (from `pom.xml`)

- `spring-boot-starter-parent` **4.0.1**, Java 17
- `spring-boot-starter-data-jpa`, `spring-boot-starter-webmvc`, `spring-boot-starter-security`, `spring-boot-starter-validation`, `spring-boot-starter-mail`
- `postgresql` driver
- `flyway-core` (schema migrations)
- `jjwt-api`/`jjwt-impl`/`jjwt-jackson` 0.11.5 — hand-rolled JWT issuing/parsing (no Spring Authorization Server / OAuth2 resource server starter)
- `twilio` 9.15.0 — SMS/phone OTP (`TwilioConfig`, `TwilioOTPService`)
- AWS `s3` SDK 2.25.60 — document/image storage (`S3Config`, `S3Service`)
- `poi`/`poi-ooxml` 5.2.5 + `commons-csv` — Excel/CSV bulk product import
- `kernel`/`layout`/`io` (iText) 7.2.5 — PDF generation (invoices, seller approval letters — `PdfService`)
- `springdoc-openapi-starter-webmvc-ui` 2.3.0 — Swagger UI at `/swagger-ui`, docs at `/api-docs`
- `lombok`
- No messaging broker dependency of any kind (no Kafka, RabbitMQ/AMQP, SQS, JMS). No caching library (no Redis/Caffeine starter). No Spring Cloud dependencies.

## Package layout (`src/main/java/com/example/pharmaaggregatorserver/`)

```
controller/   59 files   REST endpoints (~286 individual @*Mapping methods counted)
service/      ~140 files interfaces + *Impl classes, some domains flat (no interface, e.g. order/support, product/util)
repository/   ~112 files Spring Data JPA repositories (interfaces extending JpaRepository)
entity/       ~117 files JPA @Entity classes
dto/          178 files  request/response DTOs
config/       5 files    SecurityConfig, CrossConfig (CORS), S3Config, SwaggerConfig, TwilioConfig
security/     JwtUtils, AuthTokenFilter, AuthEntryPointJwt, UserDetailsServiceImpl (referenced by SecurityConfig)
```

Sub-packaging is by **business area** first (`seller`, `buyer`, `temp/seller`, `temp/buyer`, `order`, `product`, `master`, `auth`, `admin`, `quote`, `content`, `ifsc`), not by technical layer — each of `entity/`, `service/`, `repository/`, `controller/`, `dto/` repeats the same sub-folder names. Naming is inconsistent in places: some `serviceImpl` packages sit under `service/<domain>/<domain>Impl/` (e.g. `service/product/productImpl/`), others under a top-level `service/serviceImpl/<domain>/` (e.g. `service/serviceImpl/master/`, `service/serviceImpl/admin/`, `service/serviceImpl/temp/seller/`) — both patterns coexist for different domains.

## Request flow

Standard layered flow for almost every endpoint:

`Controller (@RestController, @RequestMapping)` → `Service interface` → `*ServiceImpl` (constructor-injected via Lombok `@RequiredArgsConstructor`/`private final` fields) → one or more `Repository` (Spring Data JPA) → PostgreSQL.

Cross-cutting entry points:
- `AuthTokenFilter` (registered in `SecurityConfig`, before `UsernamePasswordAuthenticationFilter`) parses the `Authorization: Bearer` JWT on every request and populates the `SecurityContext` via `UserDetailsServiceImpl`.
- `CrossConfig` registers a global `CorsFilter` allowing a fixed list of origins.
- Errors: `server.error.include-*` is turned on in dev (`application-dev.yml`) for verbose error bodies; no global `@ControllerAdvice`/`@ExceptionHandler` was found wired at the top level covering all controllers (each domain largely relies on default Spring error handling plus ad hoc try/catch in services).

Several services call **other services directly** rather than going only through repositories — e.g. `SellerApprovalServiceImpl` calls `TempSellerService`, `SellerOrderFulfillmentServiceImpl` calls `InvoiceService` and `TwilioOTPService`, `OrderPlacementServiceImpl`/`OrderCancellationServiceImpl` call `StockService` and `OrderNotificationService`. This is normal within a monolith but is exactly the coupling that has to be re-examined before any service extraction (see `api-dependency-analysis.md`).

## Counts (from Glob, reconciled during Read passes)

| Layer | Files |
|---|---|
| Controllers | 59 |
| Service interfaces + impl classes | ~140 (including support/mapper/strategy classes such as `OrderMapper`, `ProductImportStrategy*`) |
| Repositories | ~112 |
| Entities (`@Entity` classes with a table) | ~117 |
| DTOs | 178 |
| Config classes | 5 |
| REST endpoint methods (`@*Mapping`) | ~286 |

## Persistence

- Single PostgreSQL database, one schema, no read replicas or sharding configured anywhere in `application*.yml`.
- `dev` profile points at an AWS RDS instance (`pharma-aggregator-test...rds.amazonaws.com`); `prod` profile points at a `postgres-prod` host — both single-instance connection strings, no HikariCP pool tuning visible beyond Spring Boot defaults.
- Flyway migrations live under `src/main/resources/db/migration` (directory present).
- Table names follow no single convention: `tbl_*` (seller/buyer/order/temp domain tables), `tm_*` (most product master/detail tables), `pm_product_molecule` (join table), bare names like `employees`, `phone_otp`. This is evidence of the schema having grown incrementally without a naming standard being enforced.

## Server/runtime config

- `context-path: /api/v1` (matches what the frontend's `CLAUDE.md` documents as `NEXT_PUBLIC_API_URL`).
- Swagger UI enabled in dev/base, disabled in prod (`application-prod.yml`).
- Multipart upload limits: 5MB per file / 25MB per request (`application-dev.yml`), `max-part-count: 50`.
- JWT secret and access/refresh expirations are set as **plain literals in `application-dev.yml`** (`app.jwt.secret: mySecretKeyForJWTTokenGenerationAndValidation2024!@#$%^&*`), not pulled from an env var/secret manager in that profile (see `security-architecture.md`).
- AWS/Twilio/mail credentials in dev *are* externalized via `${AWS_ACCESS_KEY}`, `${ACCOUNT_SID}`, `${SPRING_MAIL_*}` etc.
