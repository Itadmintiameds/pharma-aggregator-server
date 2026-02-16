package com.example.pharmaaggregatorserver.mapper.seller;

import com.example.pharmaaggregatorserver.dto.seller.SellerCoordinatorDTO;
import com.example.pharmaaggregatorserver.entity.seller.SellerCoordinator;

public class SellerCoordinatorMapper {

    public static SellerCoordinatorDTO toDto(SellerCoordinator sellerCoordinator) {
        if (sellerCoordinator == null) return null;
        return SellerCoordinatorDTO
                .builder()
                .sellerCoordinatorId(sellerCoordinator.getSellerCoordinatorId())
                .name(sellerCoordinator.getName())
                .designation(sellerCoordinator.getDesignation())
                .email(sellerCoordinator.getEmail())
                .isEmailVerified(sellerCoordinator.isEmailVerified())
                .mobile(sellerCoordinator.getMobile())
                .isPhoneVerified(sellerCoordinator.isPhoneVerified())
                .build();
    }

    public static SellerCoordinator toEntity(SellerCoordinatorDTO sellerCoordinatorDTO) {
        if (sellerCoordinatorDTO == null) return null;
        return SellerCoordinator
                .builder()
                .sellerCoordinatorId(sellerCoordinatorDTO.getSellerCoordinatorId())
                .name(sellerCoordinatorDTO.getName())
                .designation(sellerCoordinatorDTO.getDesignation())
                .email(sellerCoordinatorDTO.getEmail())
                .isEmailVerified(sellerCoordinatorDTO.isEmailVerified())
                .mobile(sellerCoordinatorDTO.getMobile())
                .isPhoneVerified(sellerCoordinatorDTO.isPhoneVerified())
                .build();
    }
}
