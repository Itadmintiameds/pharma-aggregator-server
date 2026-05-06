package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters.HairType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface hairTypeRepository extends JpaRepository<HairType, Long> {
}
