package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.DosageForm;
import com.example.pharmaaggregatorserver.entity.product.ProductFormMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductFormMasterRepository extends JpaRepository<ProductFormMaster, Long> {

    Optional<ProductFormMaster> findByProductFormIgnoreCase(String productForm);

}
