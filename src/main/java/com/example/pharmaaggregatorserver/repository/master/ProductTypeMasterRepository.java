package com.example.pharmaaggregatorserver.repository.master;

import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductTypeMasterRepository extends JpaRepository<ProductTypeMaster, Long> {

    Optional<ProductTypeMaster> findByProductTypeNameIgnoreCase(String productTypeName);
}
