package com.example.pharmaaggregatorserver.controller.order;

import com.example.pharmaaggregatorserver.dto.order.CancelOrderRequestDTO;
import com.example.pharmaaggregatorserver.dto.order.SellerOrderActionRequestDTO;
import com.example.pharmaaggregatorserver.dto.order.SellerOrderResponseDTO;
import com.example.pharmaaggregatorserver.dto.order.ShipSellerOrderRequestDTO;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.order.OrderCancellationService;
import com.example.pharmaaggregatorserver.service.order.OrderQueryService;
import com.example.pharmaaggregatorserver.service.order.SellerOrderFulfillmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seller-orders")
@RequiredArgsConstructor
public class SellerOrderController {

    private final OrderQueryService orderQueryService;
    private final SellerOrderFulfillmentService fulfillmentService;
    private final OrderCancellationService orderCancellationService;

    @GetMapping("/{sellerOrderId}")
    public ResponseEntity<ApiResponse<SellerOrderResponseDTO>> getSellerOrder(@PathVariable String sellerOrderId) {
        SellerOrderResponseDTO response = orderQueryService.getSellerOrder(sellerOrderId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Seller order fetched successfully", response));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<List<SellerOrderResponseDTO>>> getSellerOrdersBySeller(
            @PathVariable String sellerId,
            @RequestParam(required = false) String status) {
        List<SellerOrderResponseDTO> response = orderQueryService.getSellerOrdersBySeller(sellerId, status);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(), "Seller orders fetched successfully", response, (long) response.size()));
    }

    @PatchMapping("/{sellerOrderId}/confirm")
    public ResponseEntity<ApiResponse<SellerOrderResponseDTO>> confirm(
            @PathVariable String sellerOrderId, @Valid @RequestBody SellerOrderActionRequestDTO request) {
        SellerOrderResponseDTO response = fulfillmentService.confirm(sellerOrderId, request.getSellerId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Seller order confirmed", response));
    }

    @PatchMapping("/{sellerOrderId}/pack")
    public ResponseEntity<ApiResponse<SellerOrderResponseDTO>> pack(
            @PathVariable String sellerOrderId, @Valid @RequestBody SellerOrderActionRequestDTO request) {
        SellerOrderResponseDTO response = fulfillmentService.pack(sellerOrderId, request.getSellerId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Seller order packed", response));
    }

    @PatchMapping("/{sellerOrderId}/ship")
    public ResponseEntity<ApiResponse<SellerOrderResponseDTO>> ship(
            @PathVariable String sellerOrderId, @Valid @RequestBody ShipSellerOrderRequestDTO request) {
        SellerOrderResponseDTO response = fulfillmentService.ship(
                sellerOrderId, request.getSellerId(), request.getCourierName(),
                request.getTrackingNumber(), request.getTrackingUrl());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Seller order shipped", response));
    }

    @PatchMapping("/{sellerOrderId}/out-for-delivery")
    public ResponseEntity<ApiResponse<SellerOrderResponseDTO>> outForDelivery(
            @PathVariable String sellerOrderId, @Valid @RequestBody SellerOrderActionRequestDTO request) {
        SellerOrderResponseDTO response = fulfillmentService.markOutForDelivery(sellerOrderId, request.getSellerId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Seller order marked out for delivery", response));
    }

    @PatchMapping("/{sellerOrderId}/deliver")
    public ResponseEntity<ApiResponse<SellerOrderResponseDTO>> deliver(
            @PathVariable String sellerOrderId, @Valid @RequestBody SellerOrderActionRequestDTO request) {
        SellerOrderResponseDTO response = fulfillmentService.markDelivered(sellerOrderId, request.getSellerId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Seller order delivered", response));
    }

    @PatchMapping("/{sellerOrderId}/cancel")
    public ResponseEntity<ApiResponse<SellerOrderResponseDTO>> cancel(
            @PathVariable String sellerOrderId, @Valid @RequestBody CancelOrderRequestDTO request) {
        SellerOrderResponseDTO response = orderCancellationService.cancelSellerOrder(
                sellerOrderId, request.getActorRole(), request.getActorId(), request.getReason());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Seller order cancelled", response));
    }
}
