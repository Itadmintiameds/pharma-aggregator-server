package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.ProductSubcategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductSubcategoryMasterRepository extends JpaRepository<ProductSubcategoryMaster, Long> {

    List<ProductSubcategoryMaster> findByProductCategoryMaster_ProductCategoryId(Long productCategoryId);
    Optional<ProductSubcategoryMaster> findByProductSubcategoryIgnoreCaseAndProductCategoryMaster_ProductCategoryId(
            String productSubcategory, Long productCategoryId);
}
