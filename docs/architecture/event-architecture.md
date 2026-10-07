# Event Architecture

## What exists today: none

`pom.xml` has no messaging dependency of any kind — no `spring-kafka`, no `spring-boot-starter-amqp`/RabbitMQ, no AWS SQS/SNS SDK, no JMS starter, no Spring `ApplicationEventPublisher`-based domain events were found in the service layer (only Spring's own internal context events, not custom code). All service-to-service coordination happens via **direct synchronous Java method calls** within the single deployable (e.g. `SellerApprovalServiceImpl` calling `TempSellerService`, `OrderPlacementServiceImpl` calling `StockService` and `OrderNotificationService`).

The closest thing to "async" in the codebase:
- Email sending (`EmailService`, `spring-boot-starter-mail`) — synchronous SMTP calls from within request-handling threads (no `@Async`, no queue, based on the classes read).
- SMS/OTP via Twilio (`TwilioOTPService`) — synchronous outbound HTTP call to Twilio's API.
- S3 upload (`S3Service`) — synchronous SDK call.

None of these are event buses; they are outbound integrations called inline, meaning a slow mail/SMS/S3 provider today directly slows down (or fails) the originating HTTP request.

## What would be needed for the proposed 5-service split

The boundaries in `microservice-boundaries.md` introduce several places where today's single local `@Transactional` becomes a distributed operation. These are the concrete async patterns that split would require — **all of the below is proposed/future, nothing here exists in the code today**:

### 1. Order placement saga (highest priority)
`OrderPlacementServiceImpl` today: validate buyer + delivery address → look up pricing → decrement stock (`StockService`) → create `Order`/`OrderItem`/`SellerOrder` → create `Payment` → notify (`OrderNotificationService`) — all in one local transaction.
Post-split, this needs either:
- a **choreographed saga**: Order service publishes `OrderPlacedEvent` (order id, items, buyer id) → Product Catalog service consumes it, attempts stock reservation, publishes `StockReservedEvent`/`StockReservationFailedEvent` → Order service consumes the result and finalizes or cancels the order; or
- an **orchestrated saga** with Order service (or a small saga coordinator) explicitly calling Product Catalog's reserve-stock API synchronously first (compensate with a release-stock call on downstream failure), only publishing an event for the parts that can tolerate eventual consistency (buyer notification, seller-order fulfillment kickoff).
Given the small team size implied by the codebase's structure (ASSUMPTION — no evidence of a dedicated platform/infra team), the orchestrated-with-compensation approach is simpler to reason about than a full event-choreography and is the recommended starting point.

### 2. Stock adjustment on cancellation/return
`OrderCancellationServiceImpl` and `ReturnRefundServiceImpl` both call `StockService` directly to restore stock. Post-split this is a good candidate for a genuine **async event** (`OrderCancelledEvent`/`ReturnApprovedEvent` → Product Catalog restores stock) rather than a synchronous call, because stock restoration does not block the user-facing cancel/return confirmation — eventual consistency here is acceptable.

### 3. Seller/Buyer approval → downstream notification
`SellerApprovalServiceImpl`/`BuyerApprovalServiceImpl` today call `EmailService`/`PdfService` synchronously as part of the approval request. Post-split (or even pre-split, as a resilience improvement — see `resilience-architecture.md`), these should move to an outbox-pattern event (`SellerApprovedEvent`) so the HTTP response to the admin isn't blocked on SMTP, and so a notification-service outage doesn't fail the approval transaction itself.

### 4. Product/seller read-model propagation
Per `database-ownership.md`, `ProductDetails.seller_id` and `SellerOrder.seller_id` lose their DB-level FK once Product/Order and Seller are separate databases. Any UI that needs seller display data (name, city) joined with product/order listings needs either a live API call per row (N+1 risk) or a **denormalized read-model** kept up to date via `SellerProfileUpdatedEvent`. This is a proposed pattern, not a requirement for day one — start with API calls and add the read-model only if latency/N+1 becomes a measured problem (ASSUMPTION: no current production traffic data exists to say this is needed yet).

### 5. Master/Reference data change propagation
If Master/Reference is kept as a lightweight shared service with per-consumer caching (as recommended), a `MasterDataChangedEvent` (or simply a short TTL cache with periodic refresh, given how rarely state/district/company-type data changes) is enough — this does not need a full event bus, a polling refresh is proportionate to the write frequency of these tables.

## Recommended technology (proposed only)

Given no messaging infra exists today and the team appears small: start with **Amazon SQS** (the codebase already has an AWS S3 dependency and AWS credentials wired via env vars in `application-dev.yml`, so AWS is already the deployment target — ASSUMPTION based on RDS hostname and S3 usage) for simple point-to-point async jobs (email/SMS-after-approval, stock-restore-after-cancel), reserving a broader pub/sub system (SNS fan-out, or Kafka if event volume and consumer count grow) only if/when more than 2–3 services need to react to the same event. Introducing Kafka on day one for a system this size would be over-engineering relative to the actual current entity/service count.
