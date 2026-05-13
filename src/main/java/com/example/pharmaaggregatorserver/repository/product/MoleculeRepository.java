package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.Molecule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MoleculeRepository extends JpaRepository<Molecule, Long>{

    Optional<Molecule> findByMoleculeName(String moleculeName);

    Optional<Molecule> findByMoleculeNameIgnoreCase(String moleculeName);

}
