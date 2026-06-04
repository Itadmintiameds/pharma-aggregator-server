package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.MoleculeDropdownDto;
import com.example.pharmaaggregatorserver.dto.product.MoleculeDto;

import java.util.List;

public interface MoleculeService {

    List<MoleculeDto> getAllMolecules();

    public MoleculeDto getMoleculeByName(String name);

    MoleculeDto getMoleculeById(Long moleculeId, String productAttributeId);

    List<MoleculeDropdownDto> getMoleculesByTherapeuticSubcategoryId(Long therapeuticSubcategoryId);

}
