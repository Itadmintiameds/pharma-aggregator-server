# Pharma Marketplace: Monolith → Microservices (Simple HLD)

## 1. Scope

Today the pharma marketplace backend runs as **one Spring Boot application** (this repo) with one database, covering seller onboarding, product catalog, orders, and admin approval. As the product grows, we're splitting it into **independently deployable microservices**, each owning its own data, so teams can build, deploy, and scale each part on its own.

This document covers only: why we're moving, the target architecture, who talks to whom, and the main building blocks. It intentionally skips deep implementation detail.

## 2. Why microservices

| Problem today (monolith) | What splitting fixes |
|---|---|
| One codebase, one deploy — a bug in Order can block a Product release | Each service ships independently |
| One database — Product's huge catalog and Order's transactional load compete for the same DB | Each service gets its own database, sized for its own workload |
| Can't scale parts separately — e.g. Search/Product read traffic vs Order write traffic | Scale each service independently (more instances where needed) |
| Hard to onboard new devs into one huge codebase | Smaller, focused services are easier to understand and own |

## 3. High-Level Architecture

Target: ~9 services behind a single API Gateway, each with its own database, running on AWS.

```mermaid
flowchart TB
    subgraph Client["Clients"]
        WEB[Web / Mobile App]
    end

    WEB --> ALB[AWS ALB]
    ALB --> APIGW[API Gateway Service]

    APIGW --> AUTH[Seller / Buyer Auth]
    APIGW --> SELLER[Seller Service]
    APIGW --> PRODUCT[Product Service]
    APIGW --> SEARCH[Search Service]
    APIGW --> ORDER[Order Service]
    APIGW --> QUOTE[Quotation Service]
    APIGW --> BILLING[Billing Service]
    APIGW --> NOTIFY[Notification Service]

    APIGW -.registers with.-> REGISTRY[Service Registry]
    SELLER -.registers with.-> REGISTRY
    PRODUCT -.registers with.-> REGISTRY
    ORDER -.registers with.-> REGISTRY

    CONFIG[Config Server] -. config .-> SELLER
    CONFIG -. config .-> PRODUCT
    CONFIG -. config .-> ORDER

    ORDER --> PRODUCT
    ORDER --> BILLING
    ORDER --> NOTIFY
    QUOTE --> PRODUCT
    QUOTE --> SELLER
```

## 4. System Context (AWS only)

> A colored, AWS-icon-style version of this diagram (same style as typical AWS reference architectures) is in [`aws-architecture-diagram.html`](./aws-architecture-diagram.html) — open it in a browser.

Everything runs on AWS. No third-party diagram beyond AWS-managed services.

```mermaid
flowchart LR
    USER[Buyer / Seller / Admin] -->|HTTPS| R53[Route 53]
    R53 --> CF[CloudFront]
    CF --> ALB[Application Load Balancer]
    ALB --> EKS[EKS / ECS: Microservices]

    EKS --> RDS[(RDS PostgreSQL - per service)]
    EKS --> S3[S3 - documents, invoices, product images]
    EKS --> SQS[SQS/SNS - async events]
    EKS --> SES[SES - email/OTP]
    EKS --> SECRETS[Secrets Manager]
    EKS --> CW[CloudWatch - logs & metrics]

    subgraph AWS["AWS Account"]
        CF
        ALB
        EKS
        RDS
        S3
        SQS
        SES
        SECRETS
        CW
    end
```

- **Route 53 + CloudFront**: DNS and edge caching for the frontend.
- **ALB**: routes traffic into the cluster running all microservices.
- **EKS/ECS**: hosts every microservice container (API Gateway, Auth, Seller, Product, Order, Quotation, Billing, Notification, Search, Config Server, Service Registry).
- **RDS PostgreSQL**: one database per service (not shared) — this is the core change from the monolith's single DB.
- **S3**: seller documents, invoices/PDFs, product images.
- **SQS/SNS**: async communication between services (e.g. Order → Notification, Order → Billing) instead of direct in-process calls.
- **SES**: transactional email (OTP, approvals).
- **Secrets Manager**: DB credentials, JWT secrets, third-party API keys.
- **CloudWatch**: centralized logs and metrics across all services.

## 5. Components (services)

| Service | Owns | Replaces (from monolith) |
|---|---|---|
| API Gateway | Routing, single entry point for clients | — (new) |
| Service Registry | Service discovery | — (new) |
| Config Server | Centralized config per environment | `application-*.yml` |
| Seller Service | Seller onboarding, approval, profile | `seller`, `temp/seller`, admin approval |
| Buyer / Auth | Buyer + seller login, JWT, OTP | `auth`, `SellerLogIn`, buyer auth |
| Product Service | Product catalog, categories, bulk import | `product`, `master` (product-related) |
| Search Service | Product search/browse for buyers | new, built on top of Product data |
| Quotation Service | Buyer-seller quote requests | `QuoteRequest` |
| Order Service | Orders, order status, returns | `order` |
| Billing Service | Payments, invoices, refunds | `payment`, `invoice`, `refund` |
| Notification Service | Email/SMS notifications | `EmailService`, `TwilioOTPService` |

## 6. ER Diagram (simplified, per service)

Each service owns its own tables — no shared database, no cross-service foreign keys. Cross-service links (e.g. `seller_id` on a product) are just plain IDs, validated via API call, not a DB join.

```mermaid
erDiagram
    SELLER ||--o{ PRODUCT : "sells (by seller_id, API-validated)"
    BUYER ||--o{ ORDER : places
    SELLER ||--o{ ORDER : fulfills
    ORDER ||--|{ ORDER_ITEM : contains
    PRODUCT ||--o{ ORDER_ITEM : "referenced by product_id"
    ORDER ||--o| PAYMENT : "billed via"
    ORDER ||--o| INVOICE : generates
    BUYER ||--o{ QUOTE_REQUEST : requests
    SELLER ||--o{ QUOTE_REQUEST : responds_to
    QUOTE_REQUEST ||--o| PRODUCT : "for product_id"

    SELLER {
        uuid seller_id PK
        string name
        string status
    }
    BUYER {
        uuid buyer_id PK
        string name
    }
    PRODUCT {
        uuid product_id PK
        uuid seller_id "FK by API, not DB"
        string category
    }
    ORDER {
        uuid order_id PK
        uuid buyer_id "FK by API"
        uuid seller_id "FK by API"
    }
    ORDER_ITEM {
        uuid order_item_id PK
        uuid order_id FK
        uuid product_id "FK by API"
    }
    PAYMENT {
        uuid payment_id PK
        uuid order_id "FK by API"
    }
    INVOICE {
        uuid invoice_id PK
        uuid order_id "FK by API"
    }
    QUOTE_REQUEST {
        uuid quote_id PK
        uuid buyer_id "FK by API"
        uuid seller_id "FK by API"
        uuid product_id "FK by API"
    }
```

## 7. How services communicate

Two modes: **synchronous (REST, via API Gateway or service-to-service call)** for anything that needs an immediate answer, and **asynchronous (SQS/SNS events)** for anything that can happen a moment later.

```mermaid
sequenceDiagram
    participant Buyer
    participant Gateway as API Gateway
    participant Order as Order Service
    participant Product as Product Service
    participant Billing as Billing Service
    participant Notify as Notification Service

    Buyer->>Gateway: Place order
    Gateway->>Order: POST /orders
    Order->>Product: (sync) reserve stock
    Product-->>Order: stock reserved / failed
    Order->>Order: create order + payment record
    Order-->>Gateway: order confirmed
    Gateway-->>Buyer: order confirmed

    Order->>Billing: (async event) OrderPlacedEvent via SQS
    Order->>Notify: (async event) OrderPlacedEvent via SQS
    Billing->>Billing: generate invoice
    Notify->>Buyer: email/SMS confirmation
```

**Rule of thumb:**
- **Sync (REST)**: only when the caller needs the result right now to proceed (e.g. Order needs to know stock is reserved before confirming).
- **Async (SQS/SNS event)**: everything else — notifications, invoicing, stock restore on cancel, seller-approval emails — so one slow/down service doesn't block another's request.

| From → To | Type | Example |
|---|---|---|
| Order → Product | Sync | Reserve/release stock |
| Order → Seller | Sync | Validate seller_id |
| Quotation → Product / Seller | Sync | Validate product/seller on quote |
| Order → Billing | Async | `OrderPlacedEvent` → generate invoice |
| Order → Notification | Async | `OrderPlacedEvent` / `OrderCancelledEvent` → email/SMS |
| Seller → Notification | Async | `SellerApprovedEvent` → email |
| Product → Search | Async | `ProductUpdatedEvent` → refresh search index |
| Any service → Config Server | Sync (startup) | Fetch config on boot |
| Any service → Service Registry | Sync (startup) | Register/discover |

## 8. Migration approach (short version)

We migrate incrementally, service by service (strangler-fig pattern), not a big-bang rewrite:
1. Extract low-risk shared/reference data first.
2. Extract Auth (everyone else depends on it).
3. Extract Seller and Buyer/Onboarding.
4. Extract Product Catalog.
5. Extract Order/Billing last (most dependent on everything else).
6. Retire the monolith once all services are live and the frontend is cut over.
