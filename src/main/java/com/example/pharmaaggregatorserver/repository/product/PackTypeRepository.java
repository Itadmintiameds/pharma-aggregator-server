package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.PackType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PackTypeRepository extends JpaRepository<PackType, Long> {

    List<PackType> findByDosageForm_DosageId(Long dosageId);

    Optional<PackType> findByPackType(String packType);

    Optional<PackType> findByPackTypeAndDosageForm_DosageName(String packType, String dosageName);

    List<PackType> findByCategory_CategoryId(Long categoryId);
}
