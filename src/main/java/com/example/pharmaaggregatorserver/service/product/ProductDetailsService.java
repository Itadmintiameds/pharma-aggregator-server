package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.PackagingDetailsDto;
import com.example.pharmaaggregatorserver.dto.product.ProductDetailsDto;
import com.example.pharmaaggregatorserver.dto.product.TherapeuticSubcategoryDto;

import java.util.List;

public interface ProductDetailsService {

    /**
     * @param allowMergeIntoExisting when true, a product matching this seller + product name +
     *                               manufacturer name + category is treated as another pack-size/
     *                               batch variant and merged into instead of creating a duplicate
     *                               product row (used by bulk Excel/CSV import, where multiple rows
     *                               legitimately represent the same product). Manual single-product
     *                               creation should pass false so every submission becomes its own
     *                               product, even if the name/manufacturer/category match another.
     */
    ProductDetailsDto createProduct(ProductDetailsDto dto, Long userId, boolean allowMergeIntoExisting);

    List<ProductDetailsDto> getAllProducts(Long userId);

    List<ProductDetailsDto> getAllProductsForAdmin();
    ProductDetailsDto getProductById(String productId, Long userId);

    void deleteProductById(String productId, Long userId);

    ProductDetailsDto updateProduct(String productId, ProductDetailsDto dto, Long userId);

    List<TherapeuticSubcategoryDto> getSubcategories(String categoryId);

    /**
     * Standalone step 1 of "create packaging, then add a batch against it": resolves-or-creates
     * a packaging/pack-size variant on an existing product and returns it (with its packagingId),
     * without requiring a batch/pricing entry in the same call. Pass the returned packagingId to
     * a stock-in/debit call afterward.
     */
    PackagingDetailsDto addPackagingVariant(String productId, PackagingDetailsDto dto, Long userId);

}
