package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.BatchAvailabilityDto;
import com.example.pharmaaggregatorserver.dto.product.BatchStockInDto;
import com.example.pharmaaggregatorserver.dto.product.MultiBatchStockInRequestDto;
import com.example.pharmaaggregatorserver.dto.product.StockDebitRequestDto;
import com.example.pharmaaggregatorserver.dto.product.StockInRequestDto;
import com.example.pharmaaggregatorserver.dto.product.StockLedgerResponseDto;
import com.example.pharmaaggregatorserver.entity.product.PricingDetails;
import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import com.example.pharmaaggregatorserver.entity.product.StockLedger;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.enums.StockTransactionType;
import com.example.pharmaaggregatorserver.exception.BadRequestException;
import com.example.pharmaaggregatorserver.exception.InsufficientStockException;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.exception.UnauthorizedException;
import com.example.pharmaaggregatorserver.repository.product.PricingDetailsRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductDetailsRepository;
import com.example.pharmaaggregatorserver.repository.product.StockLedgerRepository;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import com.example.pharmaaggregatorserver.service.product.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final PricingDetailsRepository pricingDetailsRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final ProductDetailsRepository productDetailsRepository;
    private final SellerRepository sellerRepository;

    @Override
    @Transactional
    public StockLedgerResponseDto addStock(StockInRequestDto request, Long userId) {

        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found for this user"));

        ProductDetails product = productDetailsRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));

        if (!product.getSeller().getSellerId().equals(seller.getSellerId())) {
            throw new UnauthorizedException("This product does not belong to the logged-in seller");
        }

        return addSingleBatch(
                product, seller, userId,
                request.getBatchLotNumber(),
                request.getManufacturingDate(),
                request.getExpiryDate(),
                request.getQuantity(),
                request.getMrp(),
                request.getSellingPrice(),
                request.getReferenceId(),
                request.getReferenceType()
        );
    }

    @Override
    @Transactional
    public List<StockLedgerResponseDto> addMultipleBatches(MultiBatchStockInRequestDto request, Long userId) {

        if (request.getBatches() == null || request.getBatches().isEmpty()) {
            throw new BadRequestException("At least one batch is required");
        }

        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found for this user"));

        ProductDetails product = productDetailsRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));

        if (!product.getSeller().getSellerId().equals(seller.getSellerId())) {
            throw new UnauthorizedException("This product does not belong to the logged-in seller");
        }

        List<StockLedgerResponseDto> results = new ArrayList<>();
        for (BatchStockInDto batchLine : request.getBatches()) {
            results.add(addSingleBatch(
                    product, seller, userId,
                    batchLine.getBatchLotNumber(),
                    batchLine.getManufacturingDate(),
                    batchLine.getExpiryDate(),
                    batchLine.getQuantity(),
                    batchLine.getMrp(),
                    batchLine.getSellingPrice(),
                    batchLine.getReferenceId(),
                    batchLine.getReferenceType()
            ));
        }
        return results;
    }

    private StockLedgerResponseDto addSingleBatch(
            ProductDetails product,
            Seller seller,
            Long userId,
            String batchLotNumber,
            LocalDateTime manufacturingDate,
            LocalDateTime expiryDate,
            Long quantity,
            BigDecimal mrp,
            BigDecimal sellingPrice,
            String referenceId,
            String referenceType
    ) {
        PricingDetails batch = pricingDetailsRepository
                .findByProductDetails_ProductIdAndBatchLotNumber(product.getProductId(), batchLotNumber)
                .orElse(null);

        if (batch != null) {
            // Existing batch — restock it. Guard against a lot number being reused with a different expiry,
            // which usually means the seller mistyped it rather than genuinely topping up the same batch.
            if (!batch.getExpiryDate().equals(expiryDate)) {
                throw new BadRequestException(
                        "Batch lot number '" + batchLotNumber
                                + "' already exists for this product with a different expiry date");
            }
            batch.setStockQuantity(batch.getStockQuantity() + quantity);
            batch.setModifiedDate(LocalDateTime.now());
        } else {
            // No matching lot number — brand new batch.
            batch = new PricingDetails();
            batch.setPricingId(generatePricingId(seller.getSellerName()));
            batch.setProductDetails(product);
            batch.setBatchLotNumber(batchLotNumber);
            batch.setManufacturingDate(manufacturingDate);
            batch.setExpiryDate(expiryDate);
            batch.setStockQuantity(quantity);
            batch.setMrp(mrp);
            batch.setSellingPrice(sellingPrice);
            batch.setDateOfStockEntry(LocalDate.now());
            batch.setCreatedDate(LocalDateTime.now());
        }
        pricingDetailsRepository.save(batch);

        StockLedger ledger = buildLedgerRow(
                batch, product, seller, userId,
                StockTransactionType.STOCK_IN,
                quantity,
                batch.getStockQuantity(),
                referenceId,
                referenceType
        );
        stockLedgerRepository.save(ledger);

        return toDto(ledger);
    }

    @Override
    @Transactional
    public List<StockLedgerResponseDto> debitStock(StockDebitRequestDto request, Long userId) {

        ProductDetails product = productDetailsRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));

        Long totalAvailable = pricingDetailsRepository.getTotalStockByProductId(request.getProductId());
        if (totalAvailable == null || totalAvailable < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock for product " + request.getProductId()
                            + ": requested " + request.getQuantity() + ", available " + totalAvailable);
        }

        // Oldest manufacturingDate first = FIFO. Row lock (see repository) stops a concurrent
        // debit/restock from reading a stale stockQuantity on the same batch. Safe to use the
        // locked variant here because this method is @Transactional.
        List<PricingDetails> batches = pricingDetailsRepository
                .lockAvailableBatchesForDebit(request.getProductId(), 0L);

        long remaining = request.getQuantity();
        List<StockLedgerResponseDto> results = new ArrayList<>();

        for (PricingDetails batch : batches) {
            if (remaining == 0) {
                break;
            }
            long take = Math.min(remaining, batch.getStockQuantity());

            batch.setStockQuantity(batch.getStockQuantity() - take);
            batch.setModifiedDate(LocalDateTime.now());
            pricingDetailsRepository.save(batch);

            StockLedger ledger = buildLedgerRow(
                    batch, product, batch.getProductDetails().getSeller(), userId,
                    StockTransactionType.STOCK_OUT,
                    take,
                    batch.getStockQuantity(),
                    request.getReferenceId(),
                    request.getReferenceType()
            );
            stockLedgerRepository.save(ledger);
            results.add(toDto(ledger));

            remaining -= take;
        }

        return results;
    }

    @Override
    public Long getTotalStock(String productId) {
        return pricingDetailsRepository.getTotalStockByProductId(productId);
    }

    @Override
    public List<BatchAvailabilityDto> getAvailableBatchesFifo(String productId) {
        return pricingDetailsRepository
                .findByProductDetails_ProductIdAndStockQuantityGreaterThanOrderByManufacturingDateAsc(productId, 0L)
                .stream()
                .map(batch -> BatchAvailabilityDto.builder()
                        .pricingId(batch.getPricingId())
                        .batchLotNumber(batch.getBatchLotNumber())
                        .manufacturingDate(batch.getManufacturingDate())
                        .expiryDate(batch.getExpiryDate())
                        .stockQuantity(batch.getStockQuantity())
                        .build())
                .toList();
    }

    @Override
    public Long getTotalDebited(String productId) {
        return stockLedgerRepository.sumQuantityByProductIdAndType(productId, StockTransactionType.STOCK_OUT);
    }

    @Override
    public Long getTotalAdded(String productId) {
        return stockLedgerRepository.sumQuantityByProductIdAndType(productId, StockTransactionType.STOCK_IN);
    }

    private StockLedger buildLedgerRow(
            PricingDetails batch,
            ProductDetails product,
            Seller seller,
            Long userId,
            StockTransactionType type,
            long quantity,
            long balanceAfter,
            String referenceId,
            String referenceType
    ) {
        StockLedger ledger = new StockLedger();
        ledger.setPricingDetails(batch);
        ledger.setProductDetails(product);
        ledger.setSeller(seller);
        ledger.setPerformedBy(userId);
        ledger.setTransactionType(type);
        ledger.setQuantity(quantity);
        ledger.setBalanceAfter(balanceAfter);
        ledger.setReferenceId(referenceId);
        ledger.setReferenceType(referenceType);
        ledger.setCreatedDate(LocalDateTime.now());
        return ledger;
    }

    private StockLedgerResponseDto toDto(StockLedger ledger) {
        return StockLedgerResponseDto.builder()
                .ledgerId(ledger.getLedgerId())
                .pricingId(ledger.getPricingDetails().getPricingId())
                .batchLotNumber(ledger.getPricingDetails().getBatchLotNumber())
                .productId(ledger.getProductDetails().getProductId())
                .transactionType(ledger.getTransactionType())
                .quantity(ledger.getQuantity())
                .balanceAfter(ledger.getBalanceAfter())
                .referenceId(ledger.getReferenceId())
                .referenceType(ledger.getReferenceType())
                .createdDate(ledger.getCreatedDate())
                .build();
    }

    // Mirrors ProductDetailsServiceImpl.generatePricingId — same "BTCH" batch-id convention,
    // kept local since this codebase duplicates per-service ID generators rather than sharing one.
    private synchronized String generatePricingId(String sellerName) {
        String cleanedSeller = sellerName.replaceAll("[^a-zA-Z]", "").toUpperCase();

        String prefix = cleanedSeller.length() >= 2
                ? cleanedSeller.substring(0, 2)
                : String.format("%-2s", cleanedSeller).replace(' ', 'X');

        Integer lastNumber = pricingDetailsRepository.findMaxPricingNumber();
        int nextNumber = (lastNumber == null) ? 1 : lastNumber + 1;

        return prefix + "BTCH" + String.format("%05d", nextNumber);
    }
}
