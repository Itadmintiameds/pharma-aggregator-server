package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.BatchAvailabilityDto;
import com.example.pharmaaggregatorserver.dto.product.BatchStockInDto;
import com.example.pharmaaggregatorserver.dto.product.MultiBatchStockInRequestDto;
import com.example.pharmaaggregatorserver.dto.product.PackagingDetailsDto;
import com.example.pharmaaggregatorserver.dto.product.StockDebitRequestDto;
import com.example.pharmaaggregatorserver.dto.product.StockInRequestDto;
import com.example.pharmaaggregatorserver.dto.product.StockLedgerResponseDto;
import com.example.pharmaaggregatorserver.entity.product.PackagingDetails;
import com.example.pharmaaggregatorserver.entity.product.PricingDetails;
import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import com.example.pharmaaggregatorserver.entity.product.StockLedger;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.enums.StockTransactionType;
import com.example.pharmaaggregatorserver.exception.BadRequestException;
import com.example.pharmaaggregatorserver.exception.InsufficientStockException;
import com.example.pharmaaggregatorserver.exception.ResourceNotFoundException;
import com.example.pharmaaggregatorserver.exception.UnauthorizedException;
import com.example.pharmaaggregatorserver.mapper.product.PackagingDetailsMapper;
import com.example.pharmaaggregatorserver.repository.product.PackagingDetailsRepository;
import com.example.pharmaaggregatorserver.repository.product.PricingDetailsRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductDetailsRepository;
import com.example.pharmaaggregatorserver.repository.product.StockLedgerRepository;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import com.example.pharmaaggregatorserver.service.product.PackagingDetailsService;
import com.example.pharmaaggregatorserver.service.product.PricingDetailsService;
import com.example.pharmaaggregatorserver.service.product.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final PricingDetailsRepository pricingDetailsRepository;
    private final PackagingDetailsRepository packagingDetailsRepository;
    private final PackagingDetailsMapper packagingDetailsMapper;
    private final PricingDetailsService pricingDetailsService;
    private final PackagingDetailsService packagingDetailsService;
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
                request.getPackagingId(),
                request.getPackagingDetails(),
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
                    batchLine.getPackagingId(),
                    batchLine.getPackagingDetails(),
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
            String packagingId,
            PackagingDetailsDto packagingDetailsDto,
            String batchLotNumber,
            LocalDateTime manufacturingDate,
            LocalDateTime expiryDate,
            Long quantity,
            BigDecimal mrp,
            BigDecimal sellingPrice,
            String referenceId,
            String referenceType
    ) {
        PackagingDetails packaging;
        if (packagingId != null && !packagingId.isBlank()) {
            // Referencing an existing variant — must already exist on this product.
            packaging = resolvePackaging(product, packagingId);
        } else if (packagingDetailsDto != null) {
            // Submitting new packaging details — reuse a matching variant if one already
            // exists on this product, otherwise create it now, in the same call.
            PackagingDetails candidatePackaging = packagingDetailsMapper.toEntity(packagingDetailsDto);
            packaging = packagingDetailsService.resolveOrCreatePackaging(
                    product, candidatePackaging, seller.getSellerName(), seller.getSellerId());
            packagingDetailsRepository.save(packaging);
        } else {
            packaging = null;
        }

        PricingDetails candidate = new PricingDetails();
        candidate.setBatchLotNumber(batchLotNumber);
        candidate.setManufacturingDate(manufacturingDate);
        candidate.setExpiryDate(expiryDate);
        candidate.setStockQuantity(quantity);
        candidate.setMrp(mrp);
        candidate.setSellingPrice(sellingPrice);

        PricingDetails batch = pricingDetailsService.resolveOrCreateBatch(
                product, packaging, candidate, seller.getSellerName(), seller.getSellerId());
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

        String packagingId = request.getPackagingId();
        boolean scoped = packagingId != null && !packagingId.isBlank();

        if (scoped) {
            // Validate the variant actually belongs to this product before scoping anything to it.
            resolvePackaging(product, packagingId);
        } else if (product.getPackagingDetails() != null && product.getPackagingDetails().size() > 1) {
            throw new BadRequestException(
                    "Product " + product.getProductId()
                            + " has multiple packaging variants — packagingId is required to debit stock");
        }

        Long totalAvailable = scoped
                ? pricingDetailsRepository.getTotalStockByProductIdAndPackagingId(request.getProductId(), packagingId)
                : pricingDetailsRepository.getTotalStockByProductId(request.getProductId());

        if (totalAvailable == null || totalAvailable < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock for product " + request.getProductId()
                            + ": requested " + request.getQuantity() + ", available " + totalAvailable);
        }

        // Oldest manufacturingDate first = FIFO. Row lock (see repository) stops a concurrent
        // debit/restock from reading a stale stockQuantity on the same batch. Safe to use the
        // locked variant here because this method is @Transactional.
        List<PricingDetails> batches = scoped
                ? pricingDetailsRepository.lockAvailableBatchesForDebitByPackaging(request.getProductId(), packagingId, 0L)
                : pricingDetailsRepository.lockAvailableBatchesForDebit(request.getProductId(), 0L);

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
                .map(this::toBatchAvailabilityDto)
                .toList();
    }

    @Override
    public List<BatchAvailabilityDto> getAvailableBatchesFifo(String productId, String packagingId) {
        if (packagingId == null || packagingId.isBlank()) {
            return getAvailableBatchesFifo(productId);
        }
        return pricingDetailsRepository
                .findByProductDetails_ProductIdAndPackagingDetails_PackagingIdAndStockQuantityGreaterThanOrderByManufacturingDateAsc(
                        productId, packagingId, 0L)
                .stream()
                .map(this::toBatchAvailabilityDto)
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

    // Looks up a packaging/variant and verifies it actually belongs to the given product —
    // guards against a caller passing a packagingId that exists but is for a different product.
    private PackagingDetails resolvePackaging(ProductDetails product, String packagingId) {
        if (packagingId == null || packagingId.isBlank()) {
            return null;
        }
        return packagingDetailsRepository.findById(packagingId)
                .filter(p -> p.getProductDetails().getProductId().equals(product.getProductId()))
                .orElseThrow(() -> new BadRequestException(
                        "Packaging " + packagingId + " not found on product " + product.getProductId()));
    }

    private BatchAvailabilityDto toBatchAvailabilityDto(PricingDetails batch) {
        return BatchAvailabilityDto.builder()
                .pricingId(batch.getPricingId())
                .packagingId(batch.getPackagingDetails() != null ? batch.getPackagingDetails().getPackagingId() : null)
                .batchLotNumber(batch.getBatchLotNumber())
                .manufacturingDate(batch.getManufacturingDate())
                .expiryDate(batch.getExpiryDate())
                .stockQuantity(batch.getStockQuantity())
                .build();
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
                .packagingId(ledger.getPricingDetails().getPackagingDetails() != null
                        ? ledger.getPricingDetails().getPackagingDetails().getPackagingId() : null)
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
}
