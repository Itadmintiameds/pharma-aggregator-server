package com.example.pharmaaggregatorserver.controller.order;

import com.example.pharmaaggregatorserver.dto.order.ReturnDecisionRequestDTO;
import com.example.pharmaaggregatorserver.dto.order.ReturnRequestCreateDTO;
import com.example.pharmaaggregatorserver.dto.order.ReturnResponseDTO;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.service.order.ReturnRefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnRefundService returnRefundService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReturnResponseDTO>> requestReturn(@Valid @RequestBody ReturnRequestCreateDTO request) {
        ReturnResponseDTO response = returnRefundService.requestReturn(
                request.getOrderItemId(), request.getBuyerId(), request.getReason());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.toString(), "Return requested successfully", response));
    }

    @PatchMapping("/{id}/decision")
    public ResponseEntity<ApiResponse<ReturnResponseDTO>> decideReturn(
            @PathVariable Long id, @Valid @RequestBody ReturnDecisionRequestDTO request) {
        ReturnResponseDTO response = returnRefundService.decideReturn(
                id, request.getSellerId(), Boolean.TRUE.equals(request.getApprove()), request.getComment());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Return decision recorded", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnResponseDTO>> getReturn(@PathVariable Long id) {
        ReturnResponseDTO response = returnRefundService.getReturn(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Return fetched successfully", response));
    }

    @PostMapping("/refunds/{refundId}/process")
    public ResponseEntity<ApiResponse<ReturnResponseDTO>> processRefund(@PathVariable Long refundId) {
        ReturnResponseDTO response = returnRefundService.processRefund(refundId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Refund processed", response));
    }
}
