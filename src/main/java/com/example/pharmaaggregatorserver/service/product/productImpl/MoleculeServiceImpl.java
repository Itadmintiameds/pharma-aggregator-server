package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.MoleculeDto;
import com.example.pharmaaggregatorserver.entity.product.Molecule;
import com.example.pharmaaggregatorserver.mapper.product.ProductDetailsDrugMapper;
import com.example.pharmaaggregatorserver.repository.product.MoleculeRepository;
import com.example.pharmaaggregatorserver.service.product.MoleculeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MoleculeServiceImpl implements MoleculeService {

    private final MoleculeRepository moleculeRepository;

    @Override
    public MoleculeDto saveMolecule(MoleculeDto dto) {

        moleculeRepository.findByMoleculeNameIgnoreCase(dto.getMoleculeName())
                .ifPresent(m -> {
                    throw new RuntimeException("Molecule already exists: " + dto.getMoleculeName());
                });

        Molecule molecule = ProductDetailsDrugMapper.toMoleculeEntity(dto);

        Molecule saved = moleculeRepository.save(molecule);

        return ProductDetailsDrugMapper.toMoleculeDto(saved);
    }

    @Override
    public MoleculeDto getMoleculeByName(String moleculeName) {

        Molecule molecule = moleculeRepository.findByMoleculeNameIgnoreCase(moleculeName)
                .orElseThrow(() -> new RuntimeException("Molecule not found: " + moleculeName));

        return ProductDetailsDrugMapper.toMoleculeDto(molecule);
    }

    @Override
    public List<MoleculeDto> getAllMolecules() {

        return moleculeRepository.findAll()
                .stream()
                .map(ProductDetailsDrugMapper::toMoleculeDto)
                .collect(Collectors.toList());
    }
}
