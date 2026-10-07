package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.ProductCertificateDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCertificateDocumentRepository extends JpaRepository<ProductCertificateDocument, Long> {

//    List<ProductCertificateDocument> findByNonConsumableMedical_ProductAttributeId(String productAttributeId);
//
//    List<ProductCertificateDocument> findByConsumableMedical_ProductAttributeId(String productAttributeId);
}
