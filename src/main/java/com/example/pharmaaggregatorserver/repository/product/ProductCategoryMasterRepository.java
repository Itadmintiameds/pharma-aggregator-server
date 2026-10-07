package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.ProductCategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryMasterRepository extends JpaRepository<ProductCategoryMaster, Long> {

    List<ProductCategoryMaster> findByCategory_CategoryId(Long categoryId);
    Optional<ProductCategoryMaster> findByProductCategoryIgnoreCaseAndCategory_CategoryId(String productCategory, Long categoryId);

}
