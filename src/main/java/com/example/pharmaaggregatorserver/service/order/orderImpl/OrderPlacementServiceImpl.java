package com.example.pharmaaggregatorserver.service.order.orderImpl;

import com.example.pharmaaggregatorserver.dto.order.OrderLineRequestDTO;
import com.example.pharmaaggregatorserver.dto.order.OrderResponseDTO;
import com.example.pharmaaggregatorserver.dto.order.PlaceOrderRequestDTO;
import com.example.pharmaaggregatorserver.dto.order.RejectedLineDTO;
import com.example.pharmaaggregatorserver.exception.InsufficientStockException;
import com.example.pharmaaggregatorserver.dto.product.StockDebitRequestDto;
import com.example.pharmaaggregatorserver.dto.product.StockLedgerResponseDto;
import com.example.pharmaaggregatorserver.entity.buyer.Buyer;
import com.example.pharmaaggregatorserver.entity.buyer.BuyerDeliveryAddress;
import com.example.pharmaaggregatorserver.entity.order.Order;
import com.example.pharmaaggregatorserver.entity.order.OrderItem;
import com.example.pharmaaggregatorserver.entity.order.OrderStatus;
import com.example.pharmaaggregatorserver.entity.order.OrderStatusHistory;
import com.example.pharmaaggregatorserver.entity.order.Payment;
import com.example.pharmaaggregatorserver.entity.order.PaymentStatus;
import com.example.pharmaaggregatorserver.entity.order.SellerOrder;
import com.example.pharmaaggregatorserver.entity.order.SellerOrderStatus;
import com.example.pharmaaggregatorserver.entity.product.PackagingDetails;
import com.example.pharmaaggregatorserver.entity.product.PricingDetails;
import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.exception.BadRequestException;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.repository.buyer.BuyerDeliveryAddressRepository;
import com.example.pharmaaggregatorserver.repository.buyer.BuyerRepository;
import com.example.pharmaaggregatorserver.repository.order.OrderRepository;
import com.example.pharmaaggregatorserver.repository.order.PaymentRepository;
import com.example.pharmaaggregatorserver.repository.product.PricingDetailsRepository;
import com.example.pharmaaggregatorserver.service.order.OrderPlacementService;
import com.example.pharmaaggregatorserver.service.order.support.OrderMapper;
import com.example.pharmaaggregatorserver.service.order.support.OrderNotificationService;
import com.example.pharmaaggregatorserver.service.product.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPlacementServiceImpl implements OrderPlacementService {

    private static final DateTimeFormatter DATE_PREFIX_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BuyerRepository buyerRepository;
    private final BuyerDeliveryAddressRepository buyerDeliveryAddressRepository;
    private final PricingDetailsRepository pricingDetailsRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final StockService stockService;
    private final OrderMapper orderMapper;
    private final OrderNotificationService orderNotificationService;

    /**
     * Holds a resolved cart line while orders are being grouped by seller,
     * before the actual stock debit (and therefore the actual OrderItem set,
     * which may span more batches than lines if FIFO spans multiple
     * batches) is known.
     */
    private record ResolvedLine(ProductDetails product, Seller seller, String packagingId, Integer quantity) {
    }

    @Override
    @Transactional
    public OrderResponseDTO placeOrder(PlaceOrderRequestDTO request) {
        Buyer buyer = buyerRepository.findById(request.getBuyerId())
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found: " + request.getBuyerId()));

        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw new BadRequestException("At least one cart line is required");
        }

        // No payment gateway is integrated in this build — every order is COD,
        // settled immediately at placement. Any paymentMethod field on the
        // request is ignored.
        //
        // Idempotency: generateOrderId() acquires the pg_advisory_xact_lock(98765)
        // lock for the rest of this transaction, so a concurrent placeOrder call
        // carrying the same idempotencyKey blocks here until this one commits or
        // rolls back — the recheck right after is therefore race-safe, not just
        // an optimistic best-effort check.
        String orderId = generateOrderId();

        if (StringUtils.hasText(request.getIdempotencyKey())) {
            var existingOrder = orderRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingOrder.isPresent()) {
                // rejectedLines from the original attempt aren't persisted, so a replay
                // returns the order as it stands now with an empty rejectedLines list —
                // acceptable since the caller already saw the original rejections once.
                return orderMapper.toOrderDto(existingOrder.get());
            }
        }

        // Resolve delivery address snapshot: an explicit deliveryAddressId lookup
        // takes precedence over raw address fields directly in the body (see
        // PlaceOrderRequestDTO's own comment on this either/or).
        String deliveryName = request.getDeliveryName();
        String deliveryPhone = request.getDeliveryPhone();
        String deliveryAddressLine = request.getDeliveryAddressLine();
        String deliveryCity = request.getDeliveryCity();
        String deliveryDistrict = request.getDeliveryDistrict();
        String deliveryState = request.getDeliveryState();
        String deliveryPinCode = request.getDeliveryPinCode();

        if (request.getDeliveryAddressId() != null) {
            BuyerDeliveryAddress address = buyerDeliveryAddressRepository.findById(request.getDeliveryAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Delivery address not found: " + request.getDeliveryAddressId()));
            if (!address.getBuyer().getBuyerId().equals(buyer.getBuyerId())) {
                throw new BadRequestException("Delivery address does not belong to this buyer");
            }
            deliveryName = buyer.getOrganizationName();
            deliveryPhone = null; // BuyerDeliveryAddress has no phone field of its own
            deliveryAddressLine = joinAddressLine(address);
            deliveryCity = address.getCity();
            deliveryDistrict = address.getDistrict();
            deliveryState = address.getState();
            deliveryPinCode = address.getPinCode();
        }

        // 1) Resolve every line's product/seller/packaging server-side — sellerId
        // is never trusted from the client.
        List<ResolvedLine> resolvedLines = new ArrayList<>();
        for (OrderLineRequestDTO line : request.getLines()) {
            PricingDetails pricing = pricingDetailsRepository.findById(line.getPricingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Batch/pricing not found: " + line.getPricingId()));

            ProductDetails product = pricing.getProductDetails();
            if (product == null || !product.getProductId().equals(line.getProductId())) {
                throw new BadRequestException(
                        "pricingId " + line.getPricingId() + " does not belong to productId " + line.getProductId());
            }

            PackagingDetails packaging = pricing.getPackagingDetails();
            if (packaging != null) {
                if (packaging.getMinimumOrderQuantity() != null
                        && line.getQuantity() < packaging.getMinimumOrderQuantity()) {
                    throw new BadRequestException(
                            "Quantity for product " + product.getProductId()
                                    + " is below the minimum order quantity of " + packaging.getMinimumOrderQuantity());
                }
                if (packaging.getMaximumOrderQuantity() != null
                        && line.getQuantity() > packaging.getMaximumOrderQuantity()) {
                    throw new BadRequestException(
                            "Quantity for product " + product.getProductId()
                                    + " exceeds the maximum order quantity of " + packaging.getMaximumOrderQuantity());
                }
            }

            Seller seller = product.getSeller();
            String packagingId = packaging != null ? packaging.getPackagingId() : null;
            resolvedLines.add(new ResolvedLine(product, seller, packagingId, line.getQuantity()));
        }

        // 2) Debit stock per line via the existing FIFO-locked StockService. A line
        // that can't be fulfilled is dropped and reported back via rejectedLines
        // rather than failing the whole placement — a buyer's cart spanning
        // multiple sellers should still get the fulfillable sellers' portions
        // placed even if one seller/line can't be.
        //
        // IMPORTANT: debitStock() is itself @Transactional. If it throws and that
        // exception is simply caught here, Spring still marks THIS method's ambient
        // transaction rollback-only the moment the exception crosses debitStock's
        // own proxy boundary — the catch below would appear to skip just that line,
        // but the whole placement would still fail with UnexpectedRollbackException
        // at commit time. So availability is checked up front via the non-throwing,
        // non-locking hasSufficientStock() and debitStock() is only called for lines
        // expected to succeed — the try/catch remains only as a backstop for the
        // rare race between this check and the real (locked) debit, which is a
        // genuine failure this design accepts rather than papers over.
        Map<String, List<OrderItem>> itemsBySeller = new LinkedHashMap<>();
        Map<String, Seller> sellerById = new LinkedHashMap<>();
        List<RejectedLineDTO> rejectedLines = new ArrayList<>();
        BigDecimal orderSubtotal = BigDecimal.ZERO;
        BigDecimal orderTax = BigDecimal.ZERO;
        int totalItemCount = 0;

        for (ResolvedLine resolved : resolvedLines) {
            if (!stockService.hasSufficientStock(
                    resolved.product().getProductId(), resolved.packagingId(), resolved.quantity())) {
                rejectedLines.add(new RejectedLineDTO(resolved.product().getProductId(),
                        "Insufficient stock for product " + resolved.product().getProductId()));
                continue;
            }

            StockDebitRequestDto debitRequest = new StockDebitRequestDto();
            debitRequest.setProductId(resolved.product().getProductId());
            debitRequest.setPackagingId(resolved.packagingId());
            debitRequest.setQuantity(resolved.quantity().longValue());
            debitRequest.setReferenceId(orderId);
            debitRequest.setReferenceType("ORDER_PLACEMENT");

            // No Buyer/Seller security principal exists yet to supply a numeric
            // userId here (see PlaceOrderRequestDTO's comment) — passed null;
            // StockLedger.performedBy has no not-null constraint.
            List<StockLedgerResponseDto> debitResults;
            try {
                debitResults = stockService.debitStock(debitRequest, null);
            } catch (InsufficientStockException e) {
                // Should be rare — only reachable via the race window described
                // above. The ambient transaction is already rollback-only by this
                // point (see the comment above), so this order will still fail as
                // a whole despite the catch; this at minimum gives a clear reason.
                rejectedLines.add(new RejectedLineDTO(resolved.product().getProductId(), e.getMessage()));
                continue;
            }

            String sellerId = resolved.seller().getSellerId();
            sellerById.putIfAbsent(sellerId, resolved.seller());

            for (StockLedgerResponseDto debited : debitResults) {
                PricingDetails debitedBatch = pricingDetailsRepository.findById(debited.getPricingId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Batch not found after debit: " + debited.getPricingId()));

                BigDecimal unitPrice = debitedBatch.getSellingPrice() != null
                        ? debitedBatch.getSellingPrice() : BigDecimal.ZERO;
                BigDecimal quantity = BigDecimal.valueOf(debited.getQuantity());
                BigDecimal lineGross = unitPrice.multiply(quantity);

                BigDecimal discountPct = debitedBatch.getDiscountPercentage() != null
                        ? debitedBatch.getDiscountPercentage() : BigDecimal.ZERO;
                BigDecimal discountAmount = lineGross.multiply(discountPct)
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

                BigDecimal gstPct = debitedBatch.getGstPercentage() != null
                        ? BigDecimal.valueOf(debitedBatch.getGstPercentage()) : BigDecimal.ZERO;
                BigDecimal taxAmount = lineGross.subtract(discountAmount).multiply(gstPct)
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

                BigDecimal lineTotal = lineGross.subtract(discountAmount).add(taxAmount);

                OrderItem item = new OrderItem();
                item.setProductDetails(resolved.product());
                item.setPricingDetails(debitedBatch);
                item.setProductNameSnapshot(resolved.product().getProductName());
                item.setBatchLotNumberSnapshot(debitedBatch.getBatchLotNumber());
                item.setPackagingIdSnapshot(resolved.packagingId());
                item.setQuantity(debited.getQuantity().intValue());
                item.setUnitPriceSnapshot(unitPrice);
                item.setDiscountAmount(discountAmount);
                item.setTaxAmount(taxAmount);
                item.setLineTotal(lineTotal);
                item.setItemStatus(SellerOrderStatus.PLACED);

                itemsBySeller.computeIfAbsent(sellerId, k -> new ArrayList<>()).add(item);

                orderSubtotal = orderSubtotal.add(lineGross).subtract(discountAmount);
                orderTax = orderTax.add(taxAmount);
                totalItemCount++;
            }
        }

        if (itemsBySeller.isEmpty()) {
            // Every line failed — nothing fulfillable, so there is no partial order
            // to place at all. Reject outright with the collected reasons.
            String reasons = rejectedLines.stream().map(RejectedLineDTO::getReason)
                    .reduce((a, b) -> a + "; " + b).orElse("no lines could be fulfilled");
            throw new BadRequestException("Order could not be placed: " + reasons);
        }

        // 3) Build one SellerOrder per seller group, with sequential-within-order
        // IDs — no advisory lock needed here since this sequence is scoped to a
        // single in-memory order build, not a cross-transaction global counter.
        List<SellerOrder> sellerOrders = new ArrayList<>();
        int sellerSeq = 0;
        for (Map.Entry<String, List<OrderItem>> entry : itemsBySeller.entrySet()) {
            sellerSeq++;
            String sellerOrderId = "SORD-" + orderIdSuffix(orderId) + "-" + sellerSeq;

            BigDecimal sellerSubtotal = BigDecimal.ZERO;
            BigDecimal sellerTax = BigDecimal.ZERO;
            for (OrderItem item : entry.getValue()) {
                sellerSubtotal = sellerSubtotal.add(item.getLineTotal()).subtract(item.getTaxAmount());
                sellerTax = sellerTax.add(item.getTaxAmount());
            }
            BigDecimal sellerShipping = BigDecimal.ZERO; // no shipping-fee computation wired up yet
            BigDecimal sellerGrandTotal = sellerSubtotal.add(sellerTax).add(sellerShipping);

            SellerOrder sellerOrder = new SellerOrder();
            sellerOrder.setSellerOrderId(sellerOrderId);
            sellerOrder.setSeller(sellerById.get(entry.getKey()));
            // Deviation: SellerOrderStatus has no "pending payment" state in the
            // fixed status list from the spec, so both COD and online-payment
            // SellerOrders start at PLACED immediately — see PaymentServiceImpl
            // for how the online-payment webhook path is handled given this.
            sellerOrder.setStatus(SellerOrderStatus.PLACED);
            sellerOrder.setSubtotal(sellerSubtotal);
            sellerOrder.setTaxAmount(sellerTax);
            sellerOrder.setShippingFee(sellerShipping);
            sellerOrder.setGrandTotal(sellerGrandTotal);

            for (OrderItem item : entry.getValue()) {
                item.setSellerOrder(sellerOrder);
            }
            sellerOrder.setOrderItems(entry.getValue());

            OrderStatusHistory history = new OrderStatusHistory();
            history.setSellerOrder(sellerOrder);
            history.setFromStatus(null);
            history.setToStatus(SellerOrderStatus.PLACED);
            history.setChangedByRole("BUYER");
            history.setChangedById(buyer.getBuyerId());
            history.setComment("Order placed");
            sellerOrder.setStatusHistory(new ArrayList<>(List.of(history)));

            sellerOrders.add(sellerOrder);
        }

        BigDecimal shippingTotal = BigDecimal.ZERO;
        BigDecimal grandTotal = orderSubtotal.add(orderTax).add(shippingTotal);

        Order order = new Order();
        order.setOrderId(orderId);
        order.setBuyer(buyer);
        order.setDeliveryName(deliveryName);
        order.setDeliveryPhone(deliveryPhone);
        order.setDeliveryAddressLine(deliveryAddressLine);
        order.setDeliveryCity(deliveryCity);
        order.setDeliveryDistrict(deliveryDistrict);
        order.setDeliveryState(deliveryState);
        order.setDeliveryPinCode(deliveryPinCode);
        order.setStatus(OrderStatus.PLACED);
        order.setIdempotencyKey(StringUtils.hasText(request.getIdempotencyKey()) ? request.getIdempotencyKey() : null);
        order.setItemCount(totalItemCount);
        order.setSellerOrderCount(sellerOrders.size());
        order.setSubtotal(orderSubtotal);
        order.setShippingTotal(shippingTotal);
        order.setTaxTotal(orderTax);
        order.setGrandTotal(grandTotal);
        order.setPlacedAt(LocalDateTime.now());

        for (SellerOrder so : sellerOrders) {
            so.setOrder(order);
        }
        order.setSellerOrders(sellerOrders);

        order = orderRepository.save(order);

        // 4) Create the Payment row now that the Order is persisted (Payment's
        // FK to Order is not-null, so Order must exist first). COD-only in this
        // build — no gateway/webhook integration — so the payment is recorded
        // as collected (SUCCESS) immediately; providerOrderId/providerTransactionId
        // stay null since there is no real provider callback populating them.
        Payment payment = new Payment();
        payment.setPaymentId(generatePaymentId());
        payment.setOrder(order);
        payment.setProvider("COD");
        payment.setAmount(grandTotal);
        payment.setCurrency("INR");
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        order.setPayment(payment);
        order = orderRepository.save(order);

        // Invoice generation happens on DELIVERY, not here (see
        // SellerOrderFulfillmentServiceImpl.markDelivered) — a tax invoice
        // represents goods actually supplied, and on a COD order that's only
        // true once delivery is confirmed; generating and emailing one at
        // placement would be premature for an order that could still be
        // cancelled before it ships.
        orderNotificationService.notifyOrderPlaced(order);

        return orderMapper.toOrderDto(order, rejectedLines);
    }

    private String joinAddressLine(BuyerDeliveryAddress address) {
        StringBuilder sb = new StringBuilder();
        appendIfPresent(sb, address.getBuildingNo());
        appendIfPresent(sb, address.getStreet());
        appendIfPresent(sb, address.getLandmark());
        appendIfPresent(sb, address.getTaluka());
        return sb.toString();
    }

    private void appendIfPresent(StringBuilder sb, String part) {
        if (part != null && !part.isBlank()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(part);
        }
    }

    private String orderIdSuffix(String orderId) {
        // "ORD-" is 4 chars.
        return orderId.substring(4);
    }

    private String generateOrderId() {
        orderRepository.acquireOrderIdLock();
        String prefix = "ORD-" + LocalDateTime.now().format(DATE_PREFIX_FORMAT) + "-";
        int next = orderRepository.findMaxOrderSequenceForPrefix(prefix) + 1;
        return prefix + String.format("%05d", next);
    }

    private String generatePaymentId() {
        paymentRepository.acquirePaymentIdLock();
        String prefix = "PAY-" + LocalDateTime.now().format(DATE_PREFIX_FORMAT) + "-";
        int next = paymentRepository.findMaxPaymentSequenceForPrefix(prefix) + 1;
        return prefix + String.format("%05d", next);
    }
}
