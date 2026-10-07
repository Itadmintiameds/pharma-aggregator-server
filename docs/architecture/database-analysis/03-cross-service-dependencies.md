# 03 — Cross-Service Dependencies

Every foreign key in the current schema that would cross a service boundary under the ownership model in `02-domain-ownership.md`, and how it must be handled once physical FKs are no longer possible.

| From (child) | FK column | To (parent) | From service | To service | Post-split handling |
|---|---|---|---|---|---|
| `Seller` | `user_id` | `tbl_user` | Seller | Identity | Keep as opaque ID column. Seller Service calls Identity's `/users/{id}` for auth checks — no DB join. |
| `Buyer` | `buyer_user_id` | `tbl_buyer_user` | Buyer | Identity | Same pattern — opaque FK, API call to validate. |
| `TempSeller` | `company_type_id`, `seller_type_id`, `state_id`, `district_id`, `taluka_id` | master tables | Seller | Reference Data | Plain ID columns, no DB-level FK. Seller Service holds a locally cached copy of reference data (see `02-domain-ownership.md`). |
| `TempSellerDocument`, `SellerDocument` | `product_type_id`, `document_type_id` | master tables | Seller | Reference Data | Same as above. |
| `Buyer` | `buyer_type_id` | `tbl_buyer_type_master` | Buyer | Reference Data | Same as above. |
| `ProductDetails` | `seller_id` | `tbl_seller` | Product | Seller | **Highest-risk FK.** Product Service validates `seller_id` via a synchronous API call at product-create time. Listing pages that today `JOIN` seller name/address must call Seller Service or read from a denormalized projection kept in sync via events. |
| `StockLedger` | `seller_id` | `tbl_seller` | Product | Seller | Same as above. |
| `Order` | `buyer_id` | `tbl_buyer` | Order | Buyer | Opaque FK. Delivery-address resolution (currently a direct repository call in `OrderPlacementServiceImpl`) becomes an API call to Buyer Service. |
| `SellerOrder` | `seller_id` | `tbl_seller` | Order | Seller | Opaque FK. Seller-side order dashboards use an API call or a denormalized read projection. |
| `OrderItem` | `pricing_id` | `tm_pricing_details` | Order | Product | Price lookup at order time becomes a synchronous API call. **Action item before splitting:** confirm `OrderItem` already stores a price snapshot at write time and does not rely on a live join to Product's price table — historical orders must not change price if the product's price changes later. |
| Return/Cancellation flow | (via `StockService`) | `tbl_stock_ledger` | Order | Product | Stock adjustments on cancel/return become an **async event**, not an in-process call. Order/return UX should not block on Product Service availability; stock correctness can tolerate brief eventual consistency. |
| `QuoteRequest` | `product_id`, `seller_id`, `buyer_user_id` | Product, Seller, Identity | Order (if Quotes move there) | three services | Three opaque FKs, each validated via API call at quote-creation time. |

## General rule applied to every row above

Every cross-service FK becomes:
1. An opaque ID column with no DB-enforced referential integrity.
2. A synchronous API call to validate the reference at write time.
3. For read-heavy display joins (e.g. showing seller name on a product card), either a client-side aggregation call or a denormalized projection kept eventually consistent via domain events — not a live cross-database join.

Reference/master data (option 2 in `02-domain-ownership.md`) is the one category exempt from rule 2 — it is cached locally rather than called per-request, since it is read-heavy and near-static.

## Order-service coupling is the biggest migration risk

`Order` reaches into Buyer (delivery address), Seller (order dashboards), and Product (pricing, stock) more than any other domain. Before splitting Order out, confirm order/order-item rows already snapshot the data they must not lose if the source service's data changes later (see the `OrderItem.pricing_id` action item above) — this is a prerequisite, not a nice-to-have.
