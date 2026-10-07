package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.GstPercentageMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

public interface GstPercentageMasterRepository extends JpaRepository<GstPercentageMaster, Long> {
    Optional<GstPercentageMaster> findByGstPercentageValue(BigDecimal gstPercentageValue);
}
