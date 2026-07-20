package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.BatchAvailabilityDto;
import com.example.pharmaaggregatorserver.dto.product.BatchDeleteResponseDto;
import com.example.pharmaaggregatorserver.dto.product.MultiBatchStockInRequestDto;
import com.example.pharmaaggregatorserver.dto.product.StockDebitRequestDto;
import com.example.pharmaaggregatorserver.dto.product.StockInRequestDto;
import com.example.pharmaaggregatorserver.dto.product.StockLedgerResponseDto;

import java.util.List;

public interface StockService {

    /**
     * Adds stock for a product: restocks the batch if batchLotNumber already
     * exists for this product, otherwise creates a brand-new batch.
     */
    StockLedgerResponseDto addStock(StockInRequestDto request, Long userId);

    /**
     * Adds multiple batches for a single product in one call. Each batch line is
     * processed with the same restock-or-create logic as {@link #addStock}.
     */
    List<StockLedgerResponseDto> addMultipleBatches(MultiBatchStockInRequestDto request, Long userId);

    /**
     * Debits stock for a product using FIFO (oldest manufacturingDate first),
     * spanning as many batches as needed. Returns one ledger row per batch touched.
     */
    List<StockLedgerResponseDto> debitStock(StockDebitRequestDto request, Long userId);

    Long getTotalStock(String productId);

    List<BatchAvailabilityDto> getAvailableBatchesFifo(String productId);

    /**
     * Same as {@link #getAvailableBatchesFifo(String)}, restricted to one packaging/pack-size
     * variant. Pass null/blank packagingId to fall back to the product-wide view.
     */
    List<BatchAvailabilityDto> getAvailableBatchesFifo(String productId, String packagingId);

    Long getTotalDebited(String productId);

    Long getTotalAdded(String productId);

    BatchDeleteResponseDto deleteBatch(String productId, String pricingId, Long userId);

}
