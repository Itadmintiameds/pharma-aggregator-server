package com.example.pharmaaggregatorserver.service.order;

import com.example.pharmaaggregatorserver.dto.order.SellerOrderResponseDTO;

public interface SellerOrderFulfillmentService {

    SellerOrderResponseDTO confirm(String sellerOrderId, String sellerId);

    SellerOrderResponseDTO pack(String sellerOrderId, String sellerId);

    SellerOrderResponseDTO ship(String sellerOrderId, String sellerId, String courierName, String trackingNumber, String trackingUrl);

    /**
     * Moves to OUT_FOR_DELIVERY and sends a delivery-confirmation OTP to the
     * buyer's phone — required before {@link #markDelivered} will accept the
     * transition to DELIVERED.
     */
    SellerOrderResponseDTO markOutForDelivery(String sellerOrderId, String sellerId);

    /**
     * @param otp the delivery OTP sent to the buyer when the order entered
     *            OUT_FOR_DELIVERY; validated before the DELIVERED transition
     *            is allowed, since this is the buyer's proof of receipt on a
     *            COD order.
     */
    SellerOrderResponseDTO markDelivered(String sellerOrderId, String sellerId, String otp);

    /**
     * Re-sends the delivery OTP for a SellerOrder already sitting in
     * OUT_FOR_DELIVERY, without re-running the SHIPPED→OUT_FOR_DELIVERY
     * transition (which markOutForDelivery requires and would reject since
     * it's already past SHIPPED). Needed because the original OTP send can
     * silently fail (SMS provider outage, malformed phone number, etc.) and
     * there was previously no way to recover other than the order being
     * permanently stuck unable to reach DELIVERED.
     */
    void resendDeliveryOtp(String sellerOrderId, String sellerId);
}
