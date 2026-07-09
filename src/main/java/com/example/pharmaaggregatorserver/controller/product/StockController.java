package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.BatchAvailabilityDto;
import com.example.pharmaaggregatorserver.dto.product.MultiBatchStockInRequestDto;
import com.example.pharmaaggregatorserver.dto.product.StockDebitRequestDto;
import com.example.pharmaaggregatorserver.dto.product.StockInRequestDto;
import com.example.pharmaaggregatorserver.dto.product.StockLedgerResponseDto;
import com.example.pharmaaggregatorserver.security.UserDetailsImpl;
import com.example.pharmaaggregatorserver.service.product.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/add")
    public ResponseEntity<StockLedgerResponseDto> addStock(
            @RequestBody StockInRequestDto request,
            Authentication authentication
    ) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(stockService.addStock(request, userId));
    }

    @PostMapping("/add-batches")
    public ResponseEntity<List<StockLedgerResponseDto>> addMultipleBatches(
            @RequestBody MultiBatchStockInRequestDto request,
            Authentication authentication
    ) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(stockService.addMultipleBatches(request, userId));
    }

    @PostMapping("/debit")
    public ResponseEntity<List<StockLedgerResponseDto>> debitStock(
            @RequestBody StockDebitRequestDto request,
            Authentication authentication
    ) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(stockService.debitStock(request, userId));
    }

    @GetMapping("/{productId}/total")
    public ResponseEntity<Long> getTotalStock(@PathVariable String productId) {
        return ResponseEntity.ok(stockService.getTotalStock(productId));
    }

    @GetMapping("/{productId}/batches")
    public ResponseEntity<List<BatchAvailabilityDto>> getAvailableBatches(
            @PathVariable String productId,
            @RequestParam(required = false) String packagingId
    ) {
        return ResponseEntity.ok(stockService.getAvailableBatchesFifo(productId, packagingId));
    }

    @GetMapping("/{productId}/debited-total")
    public ResponseEntity<Long> getTotalDebited(@PathVariable String productId) {
        return ResponseEntity.ok(stockService.getTotalDebited(productId));
    }

    @GetMapping("/{productId}/added-total")
    public ResponseEntity<Long> getTotalAdded(@PathVariable String productId) {
        return ResponseEntity.ok(stockService.getTotalAdded(productId));
    }
}
