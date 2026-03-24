package com.example.pharmaaggregatorserver.mapper.product;

import com.example.pharmaaggregatorserver.dto.product.PricingDetailsDto;
import com.example.pharmaaggregatorserver.entity.product.PricingDetails;
import org.springframework.stereotype.Component;

@Component
public class PricingDetailsMapper {

    public PricingDetails toEntity(PricingDetailsDto dto) {
        if (dto == null) return null;

        PricingDetails entity = new PricingDetails();
        entity.setPricingId(dto.getPricingId());
        entity.setBatchLotNumber(dto.getBatchLotNumber());
        entity.setManufacturerName(dto.getManufacturerName());
        entity.setManufacturingDate(dto.getManufacturingDate());
        entity.setExpiryDate(dto.getExpiryDate());
        entity.setStorageCondition(dto.getStorageCondition());
        entity.setStockQuantity(dto.getStockQuantity());
        entity.setPricePerUnit(dto.getPricePerUnit());
        entity.setMrp(dto.getMrp());
        entity.setDiscountPercentage(dto.getDiscountPercentage());
        entity.setGstPercentage(dto.getGstPercentage());
        entity.setMinimumPurchaseQuantity(dto.getMinimumPurchaseQuantity());
        entity.setAdditionalDiscount(dto.getAdditionalDiscount());
        entity.setFinalPrice(dto.getFinalPrice());
        entity.setHsnCode(dto.getHsnCode());
        entity.setShelfLifeMonths(dto.getShelfLifeMonths());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setModifiedBy(dto.getModifiedBy());
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setModifiedDate(dto.getModifiedDate());
        return entity;
    }

    public PricingDetailsDto toDTO(PricingDetails entity) {
        if (entity == null) return null;

        PricingDetailsDto dto = new PricingDetailsDto();
        dto.setPricingId(entity.getPricingId());
        dto.setBatchLotNumber(entity.getBatchLotNumber());
        dto.setManufacturerName(entity.getManufacturerName());
        dto.setManufacturingDate(entity.getManufacturingDate());
        dto.setExpiryDate(entity.getExpiryDate());
        dto.setStorageCondition(entity.getStorageCondition());
        dto.setStockQuantity(entity.getStockQuantity());
        dto.setPricePerUnit(entity.getPricePerUnit());
        dto.setMrp(entity.getMrp());
        dto.setDiscountPercentage(entity.getDiscountPercentage());
        dto.setGstPercentage(entity.getGstPercentage());
        dto.setMinimumPurchaseQuantity(entity.getMinimumPurchaseQuantity());
        dto.setAdditionalDiscount(entity.getAdditionalDiscount());
        dto.setFinalPrice(entity.getFinalPrice());
        dto.setHsnCode(entity.getHsnCode());
        dto.setShelfLifeMonths(entity.getShelfLifeMonths());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setModifiedBy(entity.getModifiedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setModifiedDate(entity.getModifiedDate());
        return dto;
    }
}
