package com.example.pharmaaggregatorserver.controller.order;

import com.example.pharmaaggregatorserver.dto.order.CancelOrderRequestDTO;
import com.example.pharmaaggregatorserver.dto.order.OrderResponseDTO;
import com.example.pharmaaggregatorserver.dto.order.PlaceOrderRequestDTO;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.order.OrderCancellationService;
import com.example.pharmaaggregatorserver.service.order.OrderPlacementService;
import com.example.pharmaaggregatorserver.service.order.OrderQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderPlacementService orderPlacementService;
    private final OrderCancellationService orderCancellationService;
    private final OrderQueryService orderQueryService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDTO>> placeOrder(@Valid @RequestBody PlaceOrderRequestDTO request) {
        OrderResponseDTO response = orderPlacementService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.toString(), "Order placed successfully", response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrder(@PathVariable String orderId) {
        OrderResponseDTO response = orderQueryService.getOrder(orderId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Order fetched successfully", response));
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getOrdersByBuyer(@PathVariable String buyerId) {
        List<OrderResponseDTO> response = orderQueryService.getOrdersByBuyer(buyerId);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(), "Buyer order history fetched successfully", response, (long) response.size()));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> cancelOrder(@PathVariable String orderId,
                                                                       @Valid @RequestBody CancelOrderRequestDTO request) {
        OrderResponseDTO response = orderCancellationService.cancelOrder(
                orderId, request.getActorRole(), request.getActorId(), request.getReason());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Order cancelled successfully", response));
    }
}
