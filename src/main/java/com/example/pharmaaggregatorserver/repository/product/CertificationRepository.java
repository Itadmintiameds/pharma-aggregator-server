package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    Optional<Certification> findByCertificationNameIgnoreCaseAndCategory_CategoryId(String trimmed, Long categoryId);

    List<Certification> findByCategory_CategoryId(Long categoryId);
}
