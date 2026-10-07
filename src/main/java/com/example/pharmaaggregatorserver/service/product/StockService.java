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
     * Restocks a quantity directly against a known batch (by pricingId), with no
     * batchLotNumber/expiry matching. Used when the caller already holds an exact
     * FK to the batch that was originally debited — e.g. order cancellation/return,
     * where {@code OrderItem.pricingDetails} points at the precise batch to reverse.
     */
    StockLedgerResponseDto restockExactBatch(String pricingId, Long quantity, Long userId,
                                              String referenceId, String referenceType);

    /**
     * Debits stock for a product using FIFO (oldest manufacturingDate first),
     * spanning as many batches as needed. Returns one ledger row per batch touched.
     */
    List<StockLedgerResponseDto> debitStock(StockDebitRequestDto request, Long userId);

    /**
     * Non-throwing, non-locking availability check — mirrors the same
     * total-stock lookup {@link #debitStock} itself does before it throws
     * {@code InsufficientStockException}. Callers that need to skip an
     * unfulfillable line WITHOUT letting an exception cross debitStock's own
     * {@code @Transactional} boundary (which would mark the caller's ambient
     * transaction rollback-only even if the caller catches it — see
     * OrderPlacementServiceImpl) should call this first and only call
     * debitStock for lines it says are fulfillable. A concurrent debit
     * between this check and the real one is still possible (this doesn't
     * lock anything) — that residual race can still throw from debitStock,
     * which is accepted as a rare, real failure rather than something to
     * paper over here.
     */
    boolean hasSufficientStock(String productId, String packagingId, long quantity);

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
