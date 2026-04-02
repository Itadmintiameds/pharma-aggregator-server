package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.PackagingDetailsDto;
import com.example.pharmaaggregatorserver.dto.product.ProductAttributeDrugDto;
import com.example.pharmaaggregatorserver.dto.product.ProductMoleculeDto;
import com.example.pharmaaggregatorserver.entity.product.*;
import com.example.pharmaaggregatorserver.repository.product.MoleculeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductAttributeDrugMapper {

    private final MoleculeRepository moleculeRepository;

    public ProductAttributeDrug toEntity(ProductAttributeDrugDto dto){
        if (dto == null) return null;

        ProductAttributeDrug entity = new ProductAttributeDrug();
        entity.setProductAttributeId(dto.getProductAttributeId());
        entity.setTherapeuticCategoryId(dto.getTherapeuticCategoryId());
        entity.setTherapeuticSubcategoryId(dto.getTherapeuticSubcategoryId());
        entity.setDosageForm(dto.getDosageForm());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setModifiedBy(dto.getModifiedBy());
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setModifiedDate(dto.getModifiedDate());
        if (dto.getMolecules() != null) {

            dto.getMolecules().forEach(mDto -> {

                ProductMolecule pm = new ProductMolecule();

                Molecule molecule = moleculeRepository.findById(mDto.getMoleculeId())
                        .orElseThrow(() -> new RuntimeException("Molecule not found"));

                ProductMoleculeId id = new ProductMoleculeId(
                        dto.getProductAttributeId(),
                        molecule.getMoleculeId()
                );

                pm.setId(id);
                pm.setProductAttributeDrug(entity);
                pm.setMolecule(molecule);
                pm.setStrength(mDto.getStrength());

                entity.getProductMolecules().add(pm);
            });
        }
        return entity;
    }


    public ProductAttributeDrugDto toDTO(ProductAttributeDrug entity) {
        if (entity == null) return null;

        ProductAttributeDrugDto dto = new ProductAttributeDrugDto();
        dto.setProductAttributeId(entity.getProductAttributeId());
        dto.setTherapeuticCategoryId(entity.getTherapeuticCategoryId());
        dto.setTherapeuticSubcategoryId(entity.getTherapeuticSubcategoryId());
        dto.setDosageForm(entity.getDosageForm());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setModifiedBy(entity.getModifiedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setModifiedDate(entity.getModifiedDate());

        if (entity.getProductMolecules() != null) {

            List<ProductMoleculeDto> moleculeDtos = entity.getProductMolecules()
                    .stream()
                    .map(pm -> {
                        ProductMoleculeDto mDto = new ProductMoleculeDto();
                        mDto.setMoleculeId(pm.getMolecule().getMoleculeId());
                        mDto.setStrength(pm.getStrength());
                        return mDto;
                    })
                    .collect(Collectors.toList());

            dto.setMolecules(moleculeDtos);
        }

        return dto;
    }

}
