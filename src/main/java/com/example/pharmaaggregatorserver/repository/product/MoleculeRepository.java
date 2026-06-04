package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.dto.product.MoleculeDropdownDto;
import com.example.pharmaaggregatorserver.entity.product.Molecule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MoleculeRepository extends JpaRepository<Molecule, Long>{

    Optional<Molecule> findByMoleculeName(String moleculeName);

    Optional<Molecule> findByMoleculeNameIgnoreCase(String moleculeName);

    @Query("""
       SELECT
       m.moleculeId as moleculeId,
       m.moleculeName as moleculeName
       FROM Molecule m
       WHERE m.therapeuticSubcategoryId = :therapeuticSubcategoryId
       """)
    List<MoleculeDropdownDto> findMoleculeIdAndNameByTherapeuticSubcategoryId(
            @Param("therapeuticSubcategoryId") Long therapeuticSubcategoryId);

}
