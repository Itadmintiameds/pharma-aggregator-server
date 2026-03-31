package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.PackType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackTypeRepository extends JpaRepository<PackType, Long> {

    List<PackType> findByDosageForm_DosageId(Long dosageId);

}
