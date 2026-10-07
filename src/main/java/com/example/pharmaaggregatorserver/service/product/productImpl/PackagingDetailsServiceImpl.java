package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.entity.product.PackagingDetails;
import com.example.pharmaaggregatorserver.entity.product.ProductDetails;
import com.example.pharmaaggregatorserver.repository.product.PackagingDetailsRepository;
import com.example.pharmaaggregatorserver.service.product.PackagingDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PackagingDetailsServiceImpl implements PackagingDetailsService {

    private final PackagingDetailsRepository packagingDetailsRepository;

    @Override
    @Transactional
    public PackagingDetails resolveOrCreatePackaging(
            ProductDetails product,
            PackagingDetails candidate,
            String sellerName,
            String sellerId
    ) {
        Long packId = candidate.getPackType() != null ? candidate.getPackType().getPackId() : null;

        Optional<PackagingDetails> existing = packagingDetailsRepository
                .findFirstByProductDetails_ProductIdAndPackType_PackIdAndUnitPerPackAndNumberOfPacksAndMinimumOrderQuantityAndMaximumOrderQuantity(
                        product.getProductId(), packId, candidate.getUnitPerPack(), candidate.getNumberOfPacks(),
                        candidate.getMinimumOrderQuantity(), candidate.getMaximumOrderQuantity());

        if (existing.isPresent()) {
            return existing.get();
        }

        candidate.setPackagingId(generatePackagingId(sellerName));
        candidate.setProductDetails(product);
        candidate.setCreatedBy(sellerId);
        candidate.setCreatedDate(LocalDateTime.now());
        return candidate;
    }

    // Mirrors ProductDetailsServiceImpl's generator — same "PKG" packaging-id convention.
    // Kept here since this is now the single place packaging variants are actually created.
    private synchronized String generatePackagingId(String sellerName) {
        String cleanedSeller = sellerName.replaceAll("[^a-zA-Z]", "").toUpperCase();

        String prefix = cleanedSeller.length() >= 2
                ? cleanedSeller.substring(0, 2)
                : String.format("%-2s", cleanedSeller).replace(' ', 'X');

        Integer lastNumber = packagingDetailsRepository.findMaxPackagingNumber();
        int nextNumber = (lastNumber == null) ? 1 : lastNumber + 1;

        return prefix + "PKG" + String.format("%05d", nextNumber);
    }
}
