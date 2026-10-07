# Scalability Architecture

## Current posture

- **Single deployable**: one Spring Boot fat jar (`spring-boot-maven-plugin` in `pom.xml`), no evidence of container orchestration config, k8s manifests, or ECS task defs in the repo scanned (only `application-dev.yml`/`application-prod.yml`/`application-test.yml` profile files were found under `src/main/resources`). Scaling today, if done at all, would be horizontal replicas of the identical monolith behind a load balancer — ASSUMPTION, since no infra-as-code was found to confirm this is actually deployed that way.
- **Single PostgreSQL database**: both `dev` (AWS RDS, `pharma-aggregator-test...rds.amazonaws.com`) and `prod` (`postgres-prod` host) profiles point at one connection string each — no read replica config, no connection-pool tuning beyond Spring Boot/HikariCP defaults, no sharding.
- **Stateless-ish app tier**: JWT-based auth (`SessionCreationPolicy.STATELESS` in `SecurityConfig`) means the app tier itself can scale horizontally without sticky sessions — this is a genuine strength already in place.
- **No caching layer**: no Redis/Caffeine/Ehcache dependency in `pom.xml`. Every request — including read-heavy master-data lookups (state/district/taluka, product category trees) — hits PostgreSQL directly.
- **Synchronous external calls on the request path**: email (`EmailService`), SMS (`TwilioOTPService`), S3 upload (`S3Service`), and PDF generation (`PdfService`, iText) are all called inline from service methods with no async offload — a slow third party directly slows or fails the user-facing request (registration submit, order placement, invoice download).
- **Bulk import path**: Excel/CSV product import (`ExcelProductImportService`, `UniversalExcelImportService`, Apache POI) runs synchronously on the request thread for what could be a large file (multipart limit is 25MB/request per `application-dev.yml`) — this is a scalability and availability risk today: a large import ties up a request thread and, given `ddl-auto` differences aside, likely runs as one long transaction.

## Where the monolith's scaling posture would meaningfully change post-split

| Concern | Monolith today | Post-5-service-split |
|---|---|---|
| Independent scaling by load shape | Not possible — Product Catalog (read-heavy, large entity graph) and Order (write-heavy, transactional) scale together as one process | Product Catalog can scale read replicas/instance count independently of Order's transactional write capacity |
| Database contention | One PostgreSQL instance serves registration wizards, catalog browsing, and order placement — a catalog import job or a registration burst can starve order-placement latency (no evidence of connection pool partitioning) | Each service's database is isolated; a Product Catalog bulk-import no longer contends with Order's connection pool |
| Master/Reference read load | Every domain queries state/district/taluka/company-type directly on every form load — no cache | With per-service local caching of Master/Reference data (recommended in `microservice-boundaries.md`), this read load drops close to zero after warm-up |
| Bulk import blast radius | A bad/huge import can degrade the entire monolith (shared thread pool, shared DB connections) | Isolated to Product Catalog service; Order/Seller/Buyer remain unaffected |
| Notification-provider outage (email/SMS) | Can block/fail registration, approval, and order-placement requests directly (synchronous calls) | Moving these behind the async event pattern in `event-architecture.md` decouples user-facing latency from provider health, independent of the service split itself |

## ASSUMPTIONS (explicitly flagged — no production telemetry available in this repo)

- **ASSUMPTION**: current production traffic is low-to-moderate (no APM/metrics config, no load-testing artifacts, no autoscaling config found) — the scalability concerns above are structural risks, not confirmed incidents.
- **ASSUMPTION**: team size is small (inferred from inconsistent package conventions — e.g. two different `serviceImpl` package patterns coexisting, `service/Test.java` and `repository/TestReositiry.java` placeholder/typo-named files left in the tree, commented-out code blocks left in `SecurityConfig`/`CrossConfig`/entity files) — a larger platform team would typically have normalized these earlier. This affects the recommended pace of the migration roadmap (fewer, larger extraction steps rather than many small ones needing dedicated service-per-team ownership).
- **ASSUMPTION**: the `prod` datasource password (`root`) and lack of TLS/SSL parameters in the JDBC URL in `application-prod.yml` suggest this may still be a pre-production/staging environment rather than a fully hardened production deployment — treat all "prod" scaling claims here as directional, not measured.

## Recommendations independent of the microservice split

1. Add a read-through cache (Redis or in-process Caffeine) in front of Master/Reference lookups — this is the single highest-value, lowest-risk scalability improvement available today, before any service extraction.
2. Move email/SMS/PDF generation off the request thread (`@Async` + a bounded executor, or the SQS-based pattern proposed in `event-architecture.md`) — reduces tail latency on registration/approval/order endpoints regardless of whether the split ever happens.
3. Move bulk Excel import to a background job (already has the right shape — one strategy class per category — just needs to run off-thread with a status-polling endpoint instead of blocking the HTTP request).
4. Add HikariCP pool sizing and PostgreSQL read-replica configuration for prod before assuming database capacity is not a bottleneck — none is currently configured.
