package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.entity.product.PackagingDetails;
import com.example.pharmaaggregatorserver.entity.product.PricingDetails;
import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import com.example.pharmaaggregatorserver.exception.BadRequestException;
import com.example.pharmaaggregatorserver.repository.product.PricingDetailsRepository;
import com.example.pharmaaggregatorserver.service.product.PricingDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PricingDetailsServiceImpl implements PricingDetailsService {

    private final PricingDetailsRepository pricingDetailsRepository;

    @Override
    public boolean isBatchNumberExistsForSeller(
            String batchLotNumber,
            Long sellerId,
            Long categoryId
    ) {

        return pricingDetailsRepository
                .existsByBatchLotNumberAndUserIdAndCategoryId(
                        batchLotNumber,
                        sellerId,
                        categoryId
                );
    }

    @Override
    @Transactional
    public PricingDetails resolveOrCreateBatch(
            ProductDetails product,
            PackagingDetails packaging,
            PricingDetails candidate,
            String sellerName,
            String sellerId
    ) {
        if (packaging == null
                && product.getPackagingDetails() != null
                && product.getPackagingDetails().size() > 1) {
            throw new BadRequestException(
                    "Product " + product.getProductId()
                            + " has multiple packaging variants — packagingId is required to add or restock a batch");
        }

        String batchLotNumber = candidate.getBatchLotNumber();

        Optional<PricingDetails> existingOpt = (packaging != null)
                ? pricingDetailsRepository.findByProductDetails_ProductIdAndPackagingDetails_PackagingIdAndBatchLotNumber(
                        product.getProductId(), packaging.getPackagingId(), batchLotNumber)
                : pricingDetailsRepository.findByProductDetails_ProductIdAndBatchLotNumber(
                        product.getProductId(), batchLotNumber);

        if (existingOpt.isPresent()) {
            PricingDetails existing = existingOpt.get();
            if (!existing.getExpiryDate().equals(candidate.getExpiryDate())) {
                throw new BadRequestException(
                        "Batch lot number '" + batchLotNumber
                                + "' already exists for this product/variant with a different expiry date");
            }
            existing.setStockQuantity(existing.getStockQuantity() + candidate.getStockQuantity());
            existing.setModifiedDate(LocalDateTime.now());
            return existing;
        }

        candidate.setPricingId(generatePricingId(sellerName));
        candidate.setProductDetails(product);
        candidate.setPackagingDetails(packaging);
        if (candidate.getDateOfStockEntry() == null) {
            candidate.setDateOfStockEntry(LocalDate.now());
        }
        candidate.setCreatedBy(sellerId);
        candidate.setCreatedDate(LocalDateTime.now());
        return candidate;
    }

    // Mirrors ProductDetailsServiceImpl's generator — same "BTCH" batch-id convention.
    // Kept here since this is now the single place batches are actually created.
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
