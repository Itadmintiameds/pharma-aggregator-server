package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.MoleculeStrengthFormat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoleculeStrengthFormatRepository
        extends JpaRepository<MoleculeStrengthFormat, Long> {

    List<MoleculeStrengthFormat> findByDosageForm_DosageId(Long dosageId);
}
