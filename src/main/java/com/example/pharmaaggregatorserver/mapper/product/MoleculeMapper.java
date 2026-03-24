package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.MoleculeDto;
import com.example.pharmaaggregatorserver.entity.product.Molecule;
import org.springframework.stereotype.Component;

@Component
public class MoleculeMapper {

    public Molecule toEntity(MoleculeDto dto){
        if (dto == null) return null;

        Molecule entity = new Molecule();
        entity.setMoleculeId(dto.getMoleculeId());
        entity.setMoleculeName(dto.getMoleculeName());
        entity.setMechanismOfAction(dto.getMechanismOfAction());
        entity.setPrimaryUse(dto.getPrimaryUse());
        entity.setDrugSchedule(dto.getDrugSchedule());
        return entity;
    }

    public Molecule fromId(Long id) {
        if (id == null) return null;

        Molecule molecule = new Molecule();
        molecule.setMoleculeId(id);
        return molecule;
    }

    public MoleculeDto toDTO(Molecule entity) {
        if (entity == null) return null;

        MoleculeDto dto = new MoleculeDto();
        dto.setMoleculeId(entity.getMoleculeId());
        dto.setMoleculeName(entity.getMoleculeName());
        dto.setMechanismOfAction(entity.getMechanismOfAction());
        dto.setPrimaryUse(entity.getPrimaryUse());
        dto.setDrugSchedule(entity.getDrugSchedule());
        return dto;
    }
}
