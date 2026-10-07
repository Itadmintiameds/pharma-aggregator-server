package com.example.pharmaaggregatorserver.controller.order;

import com.example.pharmaaggregatorserver.dto.order.CancelOrderRequestDTO;
import com.example.pharmaaggregatorserver.dto.order.DeliverSellerOrderRequestDTO;
import com.example.pharmaaggregatorserver.dto.order.SellerOrderActionRequestDTO;
import com.example.pharmaaggregatorserver.dto.order.SellerOrderResponseDTO;
import com.example.pharmaaggregatorserver.dto.order.ShipSellerOrderRequestDTO;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.exception.UnauthorizedException;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.security.UserDetailsImpl;
import com.example.pharmaaggregatorserver.service.order.OrderCancellationService;
import com.example.pharmaaggregatorserver.service.order.OrderQueryService;
import com.example.pharmaaggregatorserver.service.order.SellerOrderFulfillmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seller-orders")
@RequiredArgsConstructor
public class SellerOrderController {

    private final OrderQueryService orderQueryService;
    private final SellerOrderFulfillmentService fulfillmentService;
    private final OrderCancellationService orderCancellationService;
    private final SellerRepository sellerRepository;

    // Every endpoint here is only ever called from the seller dashboard (see
    // sellerOrderService.ts on the frontend) — the caller's own sellerId is always
    // resolved from the authenticated JWT principal rather than trusted from the
    // path/body, so one seller can no longer view or act on another seller's
    // orders just by supplying a different id in the request (previously the
    // sellerId/actorId was taken as-is from client input).
    private String resolveAuthenticatedSellerId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl user)) {
            throw new UnauthorizedException("Unauthorized access");
        }
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UnauthorizedException("No seller profile found for this account"));
        return seller.getSellerId();
    }

    @GetMapping("/{sellerOrderId}")
    public ResponseEntity<ApiResponse<SellerOrderResponseDTO>> getSellerOrder(
            @PathVariable String sellerOrderId, Authentication authentication) {
        String sellerId = resolveAuthenticatedSellerId(authentication);
        SellerOrderResponseDTO response = orderQueryService.getSellerOrder(sellerOrderId);
        if (!sellerId.equals(response.getSellerId())) {
            throw new UnauthorizedException("Seller order " + sellerOrderId + " does not belong to seller " + sellerId);
        }
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Seller order fetched successfully", response));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<List<SellerOrderResponseDTO>>> getSellerOrdersBySeller(
            @PathVariable String sellerId,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        String authenticatedSellerId = resolveAuthenticatedSellerId(authentication);
        List<SellerOrderResponseDTO> response = orderQueryService.getSellerOrdersBySeller(authenticatedSellerId, status);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(), "Seller orders fetched successfully", response, (long) response.size()));
    }

    @PatchMapping("/{sellerOrderId}/confirm")
    public ResponseEntity<ApiResponse<SellerOrderResponseDTO>> confirm(
            @PathVariable String sellerOrderId, @Valid @RequestBody SellerOrderActionRequestDTO request,
            Authentication authentication) {
        String sellerId = resolveAuthenticatedSellerId(authentication);
        SellerOrderResponseDTO response = fulfillmentService.confirm(sellerOrderId, sellerId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Seller order confirmed", response));
    }

    @PatchMapping("/{sellerOrderId}/pack")
    public ResponseEntity<ApiResponse<SellerOrderResponseDTO>> pack(
            @PathVariable String sellerOrderId, @Valid @RequestBody SellerOrderActionRequestDTO request,
            Authentication authentication) {
        String sellerId = resolveAuthenticatedSellerId(authentication);
        SellerOrderResponseDTO response = fulfillmentService.pack(sellerOrderId, sellerId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Seller order packed", response));
    }

    @PatchMapping("/{sellerOrderId}/ship")
    public ResponseEntity<ApiResponse<SellerOrderResponseDTO>> ship(
            @PathVariable String sellerOrderId, @Valid @RequestBody ShipSellerOrderRequestDTO request,
            Authentication authentication) {
        String sellerId = resolveAuthenticatedSellerId(authentication);
        SellerOrderResponseDTO response = fulfillmentService.ship(
                sellerOrderId, sellerId, request.getCourierName(),
                request.getTrackingNumber(), request.getTrackingUrl());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Seller order shipped", response));
    }

    @PatchMapping("/{sellerOrderId}/out-for-delivery")
    public ResponseEntity<ApiResponse<SellerOrderResponseDTO>> outForDelivery(
            @PathVariable String sellerOrderId, @Valid @RequestBody SellerOrderActionRequestDTO request,
            Authentication authentication) {
        String sellerId = resolveAuthenticatedSellerId(authentication);
        SellerOrderResponseDTO response = fulfillmentService.markOutForDelivery(sellerOrderId, sellerId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Seller order marked out for delivery", response));
    }

    @PatchMapping("/{sellerOrderId}/deliver")
    public ResponseEntity<ApiResponse<SellerOrderResponseDTO>> deliver(
            @PathVariable String sellerOrderId, @Valid @RequestBody DeliverSellerOrderRequestDTO request,
            Authentication authentication) {
        String sellerId = resolveAuthenticatedSellerId(authentication);
        SellerOrderResponseDTO response = fulfillmentService.markDelivered(
                sellerOrderId, sellerId, request.getOtp());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Seller order delivered", response));
    }

    @PatchMapping("/{sellerOrderId}/resend-delivery-otp")
    public ResponseEntity<ApiResponse<Void>> resendDeliveryOtp(
            @PathVariable String sellerOrderId, @Valid @RequestBody SellerOrderActionRequestDTO request,
            Authentication authentication) {
        String sellerId = resolveAuthenticatedSellerId(authentication);
        fulfillmentService.resendDeliveryOtp(sellerOrderId, sellerId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Delivery OTP resent", null));
    }

    @PatchMapping("/{sellerOrderId}/cancel")
    public ResponseEntity<ApiResponse<SellerOrderResponseDTO>> cancel(
            @PathVariable String sellerOrderId, @Valid @RequestBody CancelOrderRequestDTO request,
            Authentication authentication) {
        String sellerId = resolveAuthenticatedSellerId(authentication);
        SellerOrderResponseDTO response = orderCancellationService.cancelSellerOrder(
                sellerOrderId, "SELLER", sellerId, request.getReason());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Seller order cancelled", response));
    }
}
