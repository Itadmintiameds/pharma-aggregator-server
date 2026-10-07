package com.example.pharmaaggregatorserver.controller.admin;

import com.example.pharmaaggregatorserver.dto.order.AdminOrderOverrideRequestDTO;
import com.example.pharmaaggregatorserver.dto.order.OrderResponseDTO;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.order.OrderQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderQueryService orderQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getAllOrders(
            @RequestParam(required = false) String status) {
        List<OrderResponseDTO> response = orderQueryService.getAllOrders(status);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(), "Orders fetched successfully", response, (long) response.size()));
    }

    @PostMapping("/{orderId}/override")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> overrideOrderStatus(
            @PathVariable String orderId, @Valid @RequestBody AdminOrderOverrideRequestDTO request) {
        OrderResponseDTO response = orderQueryService.adminOverride(
                orderId, request.getNewStatus(), request.getReason(), request.getAdminId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Order status overridden", response));
    }
}
