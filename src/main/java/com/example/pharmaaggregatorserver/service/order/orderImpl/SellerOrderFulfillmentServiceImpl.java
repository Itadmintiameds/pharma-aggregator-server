package com.example.pharmaaggregatorserver.service.order.orderImpl;

import com.example.pharmaaggregatorserver.dto.order.SellerOrderResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.SMSOtpRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.SMSVerifyOtpRequestDTO;
import com.example.pharmaaggregatorserver.entity.order.Order;
import com.example.pharmaaggregatorserver.entity.order.OrderStatusHistory;
import com.example.pharmaaggregatorserver.entity.order.SellerOrder;
import com.example.pharmaaggregatorserver.entity.order.SellerOrderStatus;
import com.example.pharmaaggregatorserver.exception.BadRequestException;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.exception.UnauthorizedException;
import com.example.pharmaaggregatorserver.repository.order.OrderRepository;
import com.example.pharmaaggregatorserver.repository.order.SellerOrderRepository;
import com.example.pharmaaggregatorserver.service.EmailService;
import com.example.pharmaaggregatorserver.service.order.InvoiceService;
import com.example.pharmaaggregatorserver.service.order.SellerOrderFulfillmentService;
import com.example.pharmaaggregatorserver.service.order.support.InvoicePdfResult;
import com.example.pharmaaggregatorserver.service.order.support.OrderMapper;
import com.example.pharmaaggregatorserver.service.order.support.OrderNotificationService;
import com.example.pharmaaggregatorserver.service.order.support.OrderStatusRollup;
import com.example.pharmaaggregatorserver.service.temp.seller.TwilioOTPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerOrderFulfillmentServiceImpl implements SellerOrderFulfillmentService {

    private final SellerOrderRepository sellerOrderRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderNotificationService orderNotificationService;
    private final TwilioOTPService twilioOTPService;
    private final InvoiceService invoiceService;

    @Override
    @Transactional
    public SellerOrderResponseDTO confirm(String sellerOrderId, String sellerId) {
        return transition(sellerOrderId, sellerId, SellerOrderStatus.PLACED, SellerOrderStatus.CONFIRMED,
                so -> so.setConfirmedAt(LocalDateTime.now()));
    }

    @Override
    @Transactional
    public SellerOrderResponseDTO pack(String sellerOrderId, String sellerId) {
        return transition(sellerOrderId, sellerId, SellerOrderStatus.CONFIRMED, SellerOrderStatus.PACKED, so -> {
        });
    }

    @Override
    @Transactional
    public SellerOrderResponseDTO ship(String sellerOrderId, String sellerId, String courierName,
                                        String trackingNumber, String trackingUrl) {
        return transition(sellerOrderId, sellerId, SellerOrderStatus.PACKED, SellerOrderStatus.SHIPPED, so -> {
            so.setShippedAt(LocalDateTime.now());
            so.setCourierName(courierName);
            so.setTrackingNumber(trackingNumber);
            so.setTrackingUrl(trackingUrl);
        });
    }

    @Override
    @Transactional
    public SellerOrderResponseDTO markOutForDelivery(String sellerOrderId, String sellerId) {
        SellerOrderResponseDTO response = transition(sellerOrderId, sellerId,
                SellerOrderStatus.SHIPPED, SellerOrderStatus.OUT_FOR_DELIVERY, so -> {
                });

        SellerOrder sellerOrder = sellerOrderRepository.findBySellerOrderId(sellerOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller order not found: " + sellerOrderId));
        sendDeliveryOtp(sellerOrder);

        return response;
    }

    @Override
    @Transactional
    public SellerOrderResponseDTO markDelivered(String sellerOrderId, String sellerId, String otp) {
        SellerOrder sellerOrder = sellerOrderRepository.findBySellerOrderId(sellerOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller order not found: " + sellerOrderId));

        // Validate the OTP BEFORE any state mutation — this is the buyer's proof of
        // receipt on a COD order, not just a seller-side status flip. Ownership/status
        // preconditions are still re-checked inside transition() right after.
        verifyDeliveryOtp(sellerOrder, otp);

        // Invoice generates HERE, on confirmed delivery, not at placement — a tax
        // invoice represents goods actually supplied, which for a COD order is
        // only true once delivery is confirmed. generateInvoiceWithPdfBytes() is
        // deliberately not @Transactional (see its own comment) specifically so a
        // PDF/S3 failure here — caught below — can't mark THIS method's ambient
        // transaction rollback-only and silently break the delivery confirmation
        // itself; worst case, delivery still gets recorded but without an invoice
        // attached (one can still be pulled later via GET /invoices/{id} or the
        // manual generate endpoint).
        List<EmailService.EmailAttachment> invoiceAttachments = generateInvoiceAttachment(sellerOrderId);

        return transition(sellerOrderId, sellerId, SellerOrderStatus.OUT_FOR_DELIVERY, SellerOrderStatus.DELIVERED,
                so -> so.setDeliveredAt(LocalDateTime.now()), invoiceAttachments);
    }

    @Override
    @Transactional
    public void resendDeliveryOtp(String sellerOrderId, String sellerId) {
        SellerOrder sellerOrder = sellerOrderRepository.findBySellerOrderId(sellerOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller order not found: " + sellerOrderId));

        if (!sellerOrder.getSeller().getSellerId().equals(sellerId)) {
            throw new UnauthorizedException("Seller order " + sellerOrderId + " does not belong to seller " + sellerId);
        }
        if (!SellerOrderStatus.OUT_FOR_DELIVERY.equals(sellerOrder.getStatus())) {
            throw new BadRequestException(
                    "Seller order " + sellerOrderId + " is not OUT_FOR_DELIVERY (currently "
                            + sellerOrder.getStatus() + ") — nothing to resend an OTP for");
        }

        // Not best-effort here, unlike the original send inside markOutForDelivery —
        // this IS the recovery action for when that original send silently failed,
        // so the caller needs to know if it fails again rather than getting another
        // silent no-op.
        SMSOtpRequestDTO request = new SMSOtpRequestDTO();
        request.setPhone(resolveDeliveryPhone(sellerOrder));
        twilioOTPService.sendOTP(request);
    }

    private List<EmailService.EmailAttachment> generateInvoiceAttachment(String sellerOrderId) {
        try {
            InvoicePdfResult result = invoiceService.generateInvoiceWithPdfBytes(sellerOrderId);
            if (result.pdfBytes() != null) {
                return List.of(new EmailService.EmailAttachment(
                        "Invoice-" + result.invoice().getInvoiceNumber() + ".pdf",
                        result.pdfBytes(),
                        "application/pdf"));
            }
        } catch (Exception e) {
            log.warn("Failed to generate invoice for seller order {} on delivery: {}", sellerOrderId, e.getMessage());
        }
        return List.of();
    }

    // Buyer's phone for THIS shipment — the delivery-address snapshot phone (who's
    // actually receiving the package) takes precedence over the buyer account's
    // registered contact number, since those can differ (e.g. office reception vs
    // the account holder). Falls back to the account contact number if the
    // snapshot has none (possible when the order was placed via deliveryAddressId
    // — see PlaceOrderRequestDTO/OrderPlacementServiceImpl's own note on this).
    private String resolveDeliveryPhone(SellerOrder sellerOrder) {
        Order order = sellerOrder.getOrder();
        if (order == null) {
            return null;
        }
        String phone = order.getDeliveryPhone() != null && !order.getDeliveryPhone().isBlank()
                ? order.getDeliveryPhone()
                : (order.getBuyer() != null && order.getBuyer().getContact() != null
                        ? order.getBuyer().getContact().getMobile() : null);
        return toE164India(phone);
    }

    // Twilio Verify requires E.164 (e.g. "+918780719280") — every OTHER phone-OTP
    // flow in this app (seller/buyer registration) already sends numbers with the
    // country code prepended client-side before calling the OTP endpoint, but
    // Order.deliveryPhone/BuyerContact.mobile are stored as bare 10-digit numbers.
    // Passing those straight to Twilio silently fails the sendOTP call (caught and
    // logged by the caller) and no PhoneOTP row is ever created, so verifyOTP later
    // fails with "OTP request not found" — this normalizes to match what Twilio
    // actually expects and what every other OTP send in this codebase already sends.
    private String toE164India(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        String trimmed = phone.trim();
        if (trimmed.startsWith("+")) {
            return trimmed;
        }
        String digitsOnly = trimmed.replaceAll("[^0-9]", "");
        if (digitsOnly.length() == 10) {
            return "+91" + digitsOnly;
        }
        if (digitsOnly.length() == 12 && digitsOnly.startsWith("91")) {
            return "+" + digitsOnly;
        }
        return "+" + digitsOnly;
    }

    private void sendDeliveryOtp(SellerOrder sellerOrder) {
        String phone = resolveDeliveryPhone(sellerOrder);
        if (phone == null || phone.isBlank()) {
            log.warn("No delivery phone on file for seller order {} — delivery OTP not sent", sellerOrder.getSellerOrderId());
            return;
        }
        try {
            SMSOtpRequestDTO request = new SMSOtpRequestDTO();
            request.setPhone(phone);
            twilioOTPService.sendOTP(request);
        } catch (Exception e) {
            // Best-effort, same as OrderNotificationService — an SMS provider outage
            // must not block the OUT_FOR_DELIVERY transition itself.
            log.warn("Failed to send delivery OTP for seller order {}: {}", sellerOrder.getSellerOrderId(), e.getMessage());
        }
    }

    private void verifyDeliveryOtp(SellerOrder sellerOrder, String otp) {
        String phone = resolveDeliveryPhone(sellerOrder);
        if (phone == null || phone.isBlank()) {
            throw new BadRequestException(
                    "No delivery phone on file for seller order " + sellerOrder.getSellerOrderId()
                            + " — cannot verify delivery OTP");
        }
        SMSVerifyOtpRequestDTO request = new SMSVerifyOtpRequestDTO();
        request.setPhone(phone);
        request.setOtp(otp);
        try {
            twilioOTPService.verifyOTP(request);
        } catch (ResponseStatusException e) {
            throw new BadRequestException("Invalid or expired delivery OTP: " + e.getReason());
        }
    }

    private SellerOrderResponseDTO transition(String sellerOrderId, String sellerId,
                                               String requiredCurrentStatus, String newStatus,
                                               java.util.function.Consumer<SellerOrder> mutator) {
        return transition(sellerOrderId, sellerId, requiredCurrentStatus, newStatus, mutator, List.of());
    }

    private SellerOrderResponseDTO transition(String sellerOrderId, String sellerId,
                                               String requiredCurrentStatus, String newStatus,
                                               java.util.function.Consumer<SellerOrder> mutator,
                                               List<EmailService.EmailAttachment> notificationAttachments) {
        SellerOrder sellerOrder = sellerOrderRepository.findBySellerOrderId(sellerOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller order not found: " + sellerOrderId));

        if (!sellerOrder.getSeller().getSellerId().equals(sellerId)) {
            throw new UnauthorizedException("Seller order " + sellerOrderId + " does not belong to seller " + sellerId);
        }

        if (!requiredCurrentStatus.equals(sellerOrder.getStatus())) {
            throw new BadRequestException(
                    "Seller order " + sellerOrderId + " must be " + requiredCurrentStatus
                            + " to move to " + newStatus + ", but is currently " + sellerOrder.getStatus());
        }

        String fromStatus = sellerOrder.getStatus();
        mutator.accept(sellerOrder);
        sellerOrder.setStatus(newStatus);

        // Keep each line item's status mirroring the parent SellerOrder as
        // fulfilment progresses. Only items still at fromStatus are advanced —
        // an item that has already diverged via an individual return/partial
        // cancel is left alone rather than being pulled back in sync.
        for (var item : sellerOrder.getOrderItems()) {
            if (fromStatus.equals(item.getItemStatus())) {
                item.setItemStatus(newStatus);
            }
        }

        OrderStatusHistory history = new OrderStatusHistory();
        history.setSellerOrder(sellerOrder);
        history.setFromStatus(fromStatus);
        history.setToStatus(newStatus);
        history.setChangedByRole("SELLER");
        history.setChangedById(sellerId);
        sellerOrder.getStatusHistory().add(history);

        sellerOrderRepository.save(sellerOrder);

        Order order = sellerOrder.getOrder();
        List<String> childStatuses = order.getSellerOrders().stream().map(SellerOrder::getStatus).toList();
        order.setStatus(OrderStatusRollup.compute(childStatuses));
        orderRepository.save(order);

        orderNotificationService.notifySellerOrderStatusChanged(sellerOrder, newStatus, notificationAttachments);

        return orderMapper.toSellerOrderDto(sellerOrder);
    }
}
