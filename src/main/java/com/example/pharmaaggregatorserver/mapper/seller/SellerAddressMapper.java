package com.example.pharmaaggregatorserver.mapper.seller;

import com.example.pharmaaggregatorserver.dto.seller.SellerAddressDTO;
import com.example.pharmaaggregatorserver.entity.seller.SellerAddress;

public class SellerAddressMapper {

    public static SellerAddressDTO toDto(SellerAddress sellerAddress) {
        if (sellerAddress == null) return null;
        return SellerAddressDTO
                .builder()
                .sellerAddressId(sellerAddress.getSellerAddressId())
                .stateId(sellerAddress.getState().getStateId())
                .stateName(sellerAddress.getState().getStateName())
                .districtId(sellerAddress.getDistrict().getDistrictId())
                .districtName(sellerAddress.getDistrict().getDistrictName())
                .talukaId(sellerAddress.getTaluka().getTalukaId())
                .talukaName(sellerAddress.getTaluka().getTalukaName())
                .city(sellerAddress.getCity())
                .street(sellerAddress.getStreet())
                .buildingNo(sellerAddress.getBuildingNo())
                .landmark(sellerAddress.getLandmark())
                .pinCode(sellerAddress.getPinCode())
                .build();
    }

    public static SellerAddress toEntity(SellerAddressDTO sellerAddressDTO) {
        if (sellerAddressDTO == null) return null;
        return SellerAddress
                .builder()
                .sellerAddressId(sellerAddressDTO.getSellerAddressId())
                .city(sellerAddressDTO.getCity())
                .street(sellerAddressDTO.getStreet())
                .buildingNo(sellerAddressDTO.getBuildingNo())
                .landmark(sellerAddressDTO.getLandmark())
                .pinCode(sellerAddressDTO.getPinCode())
                .build();
    }
}
