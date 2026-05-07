package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters.SkinType;

import org.springframework.data.jpa.repository.JpaRepository;
public interface skinTypeRepository extends JpaRepository<SkinType, Long>{
}
