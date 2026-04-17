package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.MoleculeDto;
import com.example.pharmaaggregatorserver.entity.product.Molecule;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeDrug;
import com.example.pharmaaggregatorserver.mapper.product.MoleculeMapper;
import com.example.pharmaaggregatorserver.repository.product.MoleculeRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductAttributeDrugRepository;
import com.example.pharmaaggregatorserver.service.product.MoleculeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MoleculeServiceImpl implements MoleculeService {

    private final MoleculeRepository moleculeRepository;
    private final MoleculeMapper moleculeMapper;
    private final ProductAttributeDrugRepository productAttributeDrugRepository;

    @Override
    public List<MoleculeDto> getAllMolecules() {
        return moleculeRepository.findAll()
                .stream()
                .map(moleculeMapper::toDTO)
                .toList();
    }


    @Override
    public MoleculeDto getMoleculeByName(String name) {

        Molecule molecule = moleculeRepository.findByMoleculeName(name)
                .orElseThrow(() -> new RuntimeException("Molecule not found with name: " + name));

        return moleculeMapper.toDTO(molecule);
    }


    @Override
    public MoleculeDto getMoleculeById(Long moleculeId, String productAttributeId) {

        // 1. Fetch Molecule
        Molecule molecule = moleculeRepository.findById(moleculeId)
                .orElseThrow(() -> new RuntimeException("Molecule not found with id: " + moleculeId));

        // 2. Convert to DTO
        MoleculeDto dto = moleculeMapper.toDTO(molecule);

        // 3. Fetch ProductAttributeDrug (WITH molecules)
        ProductAttributeDrug productAttributeDrug = productAttributeDrugRepository
                .findById(productAttributeId)
                .orElseThrow(() -> new RuntimeException("ProductAttributeDrug not found"));

        // 4. Find matching molecule to get strength
        if (productAttributeDrug.getProductMolecules() != null) {

            productAttributeDrug.getProductMolecules().forEach(pm -> {

                if (pm.getMolecule().getMoleculeId().equals(moleculeId)) {
                    dto.setStrength(pm.getStrength()); // 🔥 ADD THIS FIELD IN DTO
                }

            });
        }

        return dto;
    }
}
