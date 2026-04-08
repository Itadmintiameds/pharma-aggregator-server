package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.MoleculeDto;
import com.example.pharmaaggregatorserver.entity.product.Molecule;
import com.example.pharmaaggregatorserver.mapper.product.MoleculeMapper;
import com.example.pharmaaggregatorserver.repository.product.MoleculeRepository;
import com.example.pharmaaggregatorserver.service.product.MoleculeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MoleculeServiceImpl implements MoleculeService {

    private final MoleculeRepository moleculeRepository;
    private final MoleculeMapper moleculeMapper;

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


}
