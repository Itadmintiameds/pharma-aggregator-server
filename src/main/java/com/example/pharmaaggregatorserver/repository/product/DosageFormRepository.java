package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.DosageForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DosageFormRepository extends JpaRepository<DosageForm, Long> {

    List<DosageForm> findByCategory_CategoryId(Long categoryId);

    Optional<DosageForm> findByDosageNameAndCategory_CategoryId(String dosageName, Long categoryId);
}
