package com.example.pharmaaggregatorserver.service.order.support;

import com.example.pharmaaggregatorserver.entity.order.Order;
import com.example.pharmaaggregatorserver.entity.order.OrderItem;
import com.example.pharmaaggregatorserver.entity.order.SellerOrder;
import com.example.pharmaaggregatorserver.entity.order.SellerOrderStatus;
import com.example.pharmaaggregatorserver.entity.product.ProductImage;
import com.example.pharmaaggregatorserver.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Best-effort branded HTML email notifications on order lifecycle events.
 * Every public method swallows its own exceptions — an SMTP outage or a
 * buyer/seller with no email on file must never fail or roll back the order
 * transaction that triggered it (placement, status transition, cancellation
 * all call this from inside their own @Transactional methods, so entity
 * relations like OrderItem.productDetails are still lazily loadable here).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationService {

    private static final String BRAND_COLOR = "#9659FD";
    private static final String BRAND_LIGHT = "#f2e9ff";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final EmailService emailService;

    public void notifyOrderPlaced(Order order) {
        sendBuyerEmail(order, "Order Confirmed - " + order.getOrderId(),
                buildOrderPlacedEmail(order));

        for (SellerOrder sellerOrder : order.getSellerOrders()) {
            sendSellerEmail(sellerOrder, "New Order Received - " + sellerOrder.getSellerOrderId(),
                    buildNewOrderForSellerEmail(order, sellerOrder));
        }
    }

    public void notifySellerOrderStatusChanged(SellerOrder sellerOrder, String newStatus) {
        notifySellerOrderStatusChanged(sellerOrder, newStatus, List.of());
    }

    /**
     * @param invoiceAttachments the seller's invoice PDF, generated on delivery
     *                           (see SellerOrderFulfillmentServiceImpl#markDelivered)
     *                           — non-empty only for the DELIVERED transition,
     *                           and only if invoice generation succeeded.
     */
    public void notifySellerOrderStatusChanged(SellerOrder sellerOrder, String newStatus,
                                                List<EmailService.EmailAttachment> invoiceAttachments) {
        String label = statusLabel(newStatus);
        if (label == null) {
            return;
        }

        Order order = sellerOrder.getOrder();
        sendBuyerEmail(order, label + " - " + sellerOrder.getSellerOrderId(),
                buildStatusUpdateEmail(order, sellerOrder, newStatus, label, !invoiceAttachments.isEmpty()),
                invoiceAttachments);
    }

    private String statusLabel(String status) {
        return switch (status) {
            case SellerOrderStatus.CONFIRMED -> "Order Confirmed";
            case SellerOrderStatus.SHIPPED -> "Order Shipped";
            case SellerOrderStatus.OUT_FOR_DELIVERY -> "Order Out for Delivery";
            case SellerOrderStatus.DELIVERED -> "Order Delivered";
            case SellerOrderStatus.CANCELLED -> "Order Cancelled";
            default -> null;
        };
    }

    private String statusAccentColor(String status) {
        return switch (status) {
            case SellerOrderStatus.CANCELLED -> "#dc2626";
            case SellerOrderStatus.DELIVERED -> "#16a34a";
            default -> BRAND_COLOR;
        };
    }

    // ─────────────────────────────────────────────────────────
    // Email senders (exception-swallowing)
    // ─────────────────────────────────────────────────────────

    private void sendBuyerEmail(Order order, String subject, String html) {
        sendBuyerEmail(order, subject, html, List.of());
    }

    private void sendBuyerEmail(Order order, String subject, String html, List<EmailService.EmailAttachment> attachments) {
        try {
            String email = order != null && order.getBuyer() != null && order.getBuyer().getContact() != null
                    ? order.getBuyer().getContact().getEmail() : null;
            if (email == null || email.isBlank()) {
                return;
            }
            if (attachments == null || attachments.isEmpty()) {
                emailService.sendHtmlMail(email, subject, html);
            } else {
                emailService.sendHtmlMailWithAttachments(email, subject, html, attachments);
            }
        } catch (Exception e) {
            log.warn("Failed to send buyer order notification for order {}: {}",
                    order != null ? order.getOrderId() : "?", e.getMessage());
        }
    }

    private void sendSellerEmail(SellerOrder sellerOrder, String subject, String html) {
        try {
            String email = sellerOrder.getSeller() != null ? sellerOrder.getSeller().getEmail() : null;
            if (email != null && !email.isBlank()) {
                emailService.sendHtmlMail(email, subject, html);
            }
        } catch (Exception e) {
            log.warn("Failed to send seller order notification for seller order {}: {}",
                    sellerOrder.getSellerOrderId(), e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    // Template builders
    // ─────────────────────────────────────────────────────────

    private String buildOrderPlacedEmail(Order order) {
        List<OrderItem> allItems = order.getSellerOrders().stream()
                .flatMap(so -> so.getOrderItems().stream())
                .toList();

        String itemRows = allItems.stream().map(this::itemRow).reduce("", String::concat);
        String placedAt = order.getPlacedAt() != null ? order.getPlacedAt().format(DATE_FORMAT) : "";
        // Invoice generates on delivery (see SellerOrderFulfillmentServiceImpl),
        // not here — a tax invoice represents goods actually supplied, which for
        // a COD order is only true once delivery is confirmed.
        String invoiceNote = "Your tax invoice"
                + (order.getSellerOrderCount() != null && order.getSellerOrderCount() > 1 ? "s will" : " will")
                + " be emailed to you once your order is delivered.";

        return wrapper(
                BRAND_COLOR,
                "Thank you for your order!",
                """
                <p style="margin:0 0 20px; font-size:14px; color:#555555; line-height:1.6;">
                  Your order has been placed successfully and is being processed. Here are the details:
                </p>
                %s
                %s
                %s
                <p style="margin:20px 0 0; font-size:13px; color:#888888; line-height:1.6;">
                  %s
                </p>
                """.formatted(
                        summaryBlock(order.getOrderId(), placedAt, "Cash on Delivery"),
                        itemsTable(itemRows),
                        totalsBlock(order.getSubtotal(), order.getShippingTotal(), order.getTaxTotal(), order.getGrandTotal()),
                        invoiceNote
                ),
                deliveryAddressBlock(order)
        );
    }

    private String buildNewOrderForSellerEmail(Order order, SellerOrder sellerOrder) {
        String itemRows = sellerOrder.getOrderItems().stream().map(this::itemRow).reduce("", String::concat);

        return wrapper(
                BRAND_COLOR,
                "You've received a new order",
                """
                <p style="margin:0 0 20px; font-size:14px; color:#555555; line-height:1.6;">
                  Order <b>%s</b> (part of buyer order %s) is waiting for your confirmation.
                </p>
                %s
                %s
                <p style="margin:20px 0 0; font-size:13px; color:#888888; line-height:1.6;">
                  Please confirm this order from your seller dashboard as soon as possible.
                </p>
                """.formatted(
                        sellerOrder.getSellerOrderId(), order.getOrderId(),
                        itemsTable(itemRows),
                        totalsBlock(sellerOrder.getSubtotal(), sellerOrder.getShippingFee(), sellerOrder.getTaxAmount(), sellerOrder.getGrandTotal())
                ),
                null
        );
    }

    private String buildStatusUpdateEmail(Order order, SellerOrder sellerOrder, String newStatus, String label,
                                           boolean invoiceAttached) {
        String accent = statusAccentColor(newStatus);
        String itemRows = sellerOrder.getOrderItems().stream().map(this::itemRow).reduce("", String::concat);

        String extra = "";
        if (SellerOrderStatus.DELIVERED.equals(newStatus)) {
            extra = """
                    <p style="margin:0 0 20px; font-size:14px; color:#555555; line-height:1.6;">
                      %s
                    </p>
                    """.formatted(invoiceAttached
                    ? "Your tax invoice for this order is attached to this email as a PDF."
                    : "Thanks for shopping with us!");
        }
        if (SellerOrderStatus.SHIPPED.equals(newStatus) && sellerOrder.getTrackingNumber() != null) {
            extra = """
                    <p style="margin:0 0 20px; font-size:14px; color:#555555; line-height:1.6;">
                      Courier: <b>%s</b> &nbsp;·&nbsp; Tracking number: <b>%s</b>
                    </p>
                    """.formatted(
                    sellerOrder.getCourierName() != null ? sellerOrder.getCourierName() : "-",
                    sellerOrder.getTrackingNumber());
        }
        if (SellerOrderStatus.OUT_FOR_DELIVERY.equals(newStatus)) {
            extra = """
                    <p style="margin:0 0 20px; font-size:14px; color:#555555; line-height:1.6;">
                      A verification code has been sent to your delivery phone number — please share it
                      with the delivery person to confirm receipt.
                    </p>
                    """;
        }
        if (SellerOrderStatus.CANCELLED.equals(newStatus) && sellerOrder.getCancelReason() != null) {
            extra = """
                    <p style="margin:0 0 20px; font-size:14px; color:#555555; line-height:1.6;">
                      Reason: %s
                    </p>
                    """.formatted(sellerOrder.getCancelReason());
        }

        return wrapper(
                accent,
                label,
                """
                <p style="margin:0 0 16px; font-size:14px; color:#555555; line-height:1.6;">
                  Order <b>%s</b> (part of order %s) is now <b>%s</b>.
                </p>
                %s
                %s
                """.formatted(
                        sellerOrder.getSellerOrderId(),
                        order != null ? order.getOrderId() : "?",
                        label,
                        extra,
                        itemsTable(itemRows)
                ),
                null
        );
    }

    // ─────────────────────────────────────────────────────────
    // Shared HTML fragments
    // ─────────────────────────────────────────────────────────

    private String itemRow(OrderItem item) {
        String imageUrl = item.getProductDetails() != null && item.getProductDetails().getProductImages() != null
                ? item.getProductDetails().getProductImages().stream()
                        .map(ProductImage::getProductImage)
                        .filter(url -> url != null && !url.isBlank())
                        .findFirst().orElse(null)
                : null;

        String thumb = imageUrl != null
                ? "<img src=\"" + imageUrl + "\" width=\"48\" height=\"48\" alt=\"\" "
                        + "style=\"display:block; border-radius:6px; object-fit:cover; background-color:#f4f6f8;\" />"
                : "<div style=\"width:48px; height:48px; border-radius:6px; background-color:" + BRAND_LIGHT + "; "
                        + "color:" + BRAND_COLOR + "; font-weight:bold; font-size:16px; text-align:center; line-height:48px;\">"
                        + (item.getProductNameSnapshot() != null && !item.getProductNameSnapshot().isBlank()
                                ? item.getProductNameSnapshot().substring(0, 1).toUpperCase() : "?")
                        + "</div>";

        return """
                <tr>
                  <td style="padding:10px 0; border-bottom:1px solid #f0f0f0; width:56px;">%s</td>
                  <td style="padding:10px 12px; border-bottom:1px solid #f0f0f0;">
                    <span style="font-size:14px; color:#222222; font-weight:600;">%s</span><br/>
                    <span style="font-size:12px; color:#888888;">Qty: %d %s</span>
                  </td>
                  <td style="padding:10px 0; border-bottom:1px solid #f0f0f0; text-align:right; white-space:nowrap;">
                    <span style="font-size:14px; color:#222222; font-weight:600;">Rs. %s</span>
                  </td>
                </tr>
                """.formatted(
                thumb,
                escape(item.getProductNameSnapshot()),
                item.getQuantity(),
                item.getBatchLotNumberSnapshot() != null ? "· Batch " + escape(item.getBatchLotNumberSnapshot()) : "",
                item.getLineTotal() != null ? item.getLineTotal() : BigDecimal.ZERO
        );
    }

    private String itemsTable(String rows) {
        return """
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:20px;">
                  %s
                </table>
                """.formatted(rows);
    }

    private String summaryBlock(String orderId, String placedAt, String paymentMethod) {
        return """
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                    style="background-color:#f9f9fb; border-radius:8px; margin-bottom:20px;">
                  <tr>
                    <td style="padding:16px 20px;">
                      <p style="margin:0 0 6px; font-size:13px; color:#888888;">Order ID</p>
                      <p style="margin:0 0 14px; font-size:15px; color:#222222; font-weight:700;">%s</p>
                      <p style="margin:0 0 6px; font-size:13px; color:#888888;">Placed On</p>
                      <p style="margin:0 0 14px; font-size:14px; color:#222222;">%s</p>
                      <p style="margin:0 0 6px; font-size:13px; color:#888888;">Payment Method</p>
                      <p style="margin:0; font-size:14px; color:#222222;">%s</p>
                    </td>
                  </tr>
                </table>
                """.formatted(orderId, placedAt, paymentMethod);
    }

    private String totalsBlock(BigDecimal subtotal, BigDecimal shipping, BigDecimal tax, BigDecimal grandTotal) {
        return """
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:8px;">
                  <tr>
                    <td style="padding:4px 0; font-size:13px; color:#666666;">Subtotal</td>
                    <td style="padding:4px 0; font-size:13px; color:#666666; text-align:right;">Rs. %s</td>
                  </tr>
                  <tr>
                    <td style="padding:4px 0; font-size:13px; color:#666666;">Shipping</td>
                    <td style="padding:4px 0; font-size:13px; color:#666666; text-align:right;">Rs. %s</td>
                  </tr>
                  <tr>
                    <td style="padding:4px 0; font-size:13px; color:#666666;">Tax</td>
                    <td style="padding:4px 0; font-size:13px; color:#666666; text-align:right;">Rs. %s</td>
                  </tr>
                  <tr>
                    <td style="padding:10px 0 0; font-size:15px; color:#222222; font-weight:700; border-top:1px solid #eeeeee;">Grand Total</td>
                    <td style="padding:10px 0 0; font-size:15px; color:#222222; font-weight:700; text-align:right; border-top:1px solid #eeeeee;">Rs. %s</td>
                  </tr>
                </table>
                """.formatted(
                nz(subtotal), nz(shipping), nz(tax), nz(grandTotal)
        );
    }

    private String deliveryAddressBlock(Order order) {
        if (order.getDeliveryAddressLine() == null) {
            return "";
        }
        return """
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                    style="background-color:#f9f9fb; border-radius:8px; margin-top:8px;">
                  <tr>
                    <td style="padding:16px 20px;">
                      <p style="margin:0 0 6px; font-size:13px; color:#888888;">Delivering To</p>
                      <p style="margin:0; font-size:14px; color:#222222; line-height:1.6;">
                        %s<br/>%s<br/>%s, %s, %s - %s
                      </p>
                    </td>
                  </tr>
                </table>
                """.formatted(
                escape(order.getDeliveryName()),
                escape(order.getDeliveryPhone()),
                escape(order.getDeliveryAddressLine()),
                escape(order.getDeliveryCity()),
                escape(order.getDeliveryState()),
                escape(order.getDeliveryPinCode())
        );
    }

    private String nz(BigDecimal value) {
        return value != null ? value.toString() : "0.00";
    }

    private String escape(String value) {
        return value != null ? value : "";
    }

    /**
     * Shared branded shell — mirrors EmailService's OTP email look (purple
     * header banner, white card, light-gray footer) so every order email
     * reads as the same product rather than a bolted-on plain-text afterthought.
     */
    private String wrapper(String accentColor, String heading, String bodyHtml, String extraBlock) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0; padding:0; background-color:#f4f6f8; font-family:Arial, Helvetica, sans-serif;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f8; padding:32px 0;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="560" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:8px; overflow:hidden; box-shadow:0 1px 3px rgba(0,0,0,0.08);">
                          <tr>
                            <td style="background-color:%s; padding:24px 32px;">
                              <span style="color:#ffffff; font-size:20px; font-weight:bold; letter-spacing:0.3px;">TiaMeds Marketplace</span>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:32px;">
                              <h1 style="margin:0 0 16px; font-size:20px; color:#222222;">%s</h1>
                              %s
                              %s
                            </td>
                          </tr>
                          <tr>
                            <td style="background-color:#f4f6f8; padding:20px 32px; border-top:1px solid #e5e8eb;">
                              <p style="margin:0; font-size:12px; color:#999999; line-height:1.6;">
                                Warm Regards,<br>
                                TiaMeds Marketplace<br>
                                Buyer Support Team
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(accentColor, heading, bodyHtml, extraBlock != null ? extraBlock : "");
    }
}
