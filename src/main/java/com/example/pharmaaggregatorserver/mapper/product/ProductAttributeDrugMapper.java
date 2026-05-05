package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.ProductAttributeDrugDto;
import com.example.pharmaaggregatorserver.dto.product.ProductMoleculeDto;
import com.example.pharmaaggregatorserver.entity.product.*;
import com.example.pharmaaggregatorserver.entity.product.MedicalDeviceProductMaster.StorageConditionMaster;
import com.example.pharmaaggregatorserver.repository.product.MoleculeRepository;
import com.example.pharmaaggregatorserver.repository.product.StorageConditionMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductAttributeDrugMapper {

    private final MoleculeRepository moleculeRepository;
    private final StorageConditionMasterRepository storageConditionRepository;

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
                        .orElseThrow(() -> new RuntimeException("Molecule not found with Id: " + mDto.getMoleculeId()));

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

        if (dto.getUserManualUrl() != null) {

            ProductUserManual manual = new ProductUserManual();
            manual.setUserManualUrl(dto.getUserManualUrl());
            manual.setProductAttributeDrug(entity);
            entity.setProductUserManual(manual);
        }

        if (dto.getStorageConditionIds() != null && !dto.getStorageConditionIds().isEmpty()) {

            Set<StorageConditionMaster> conditions =
                    new HashSet<>(storageConditionRepository.findAllById(dto.getStorageConditionIds()));

            entity.setStorageConditions(conditions);
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

        if (entity.getProductUserManual() != null) {
            dto.setUserManualUrl(entity.getProductUserManual().getUserManualUrl());
        }

        if (entity.getStorageConditions() != null && !entity.getStorageConditions().isEmpty()) {

            List<Long> ids = entity.getStorageConditions()
                    .stream()
                    .map(StorageConditionMaster::getStorageConditionId)
                    .collect(Collectors.toList());

            dto.setStorageConditionIds(ids);
        }

        return dto;
    }

}
