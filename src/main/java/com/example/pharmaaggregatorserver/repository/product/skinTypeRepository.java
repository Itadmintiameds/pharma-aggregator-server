package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters.SkinType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface skinTypeRepository extends JpaRepository<SkinType, Long>{
    Optional<SkinType> findByTypeNameIgnoreCase(String typeName);
}
