package com.example.pharmaaggregatorserver.service.seller;

import com.example.pharmaaggregatorserver.dto.seller.SellerDTO;
import com.example.pharmaaggregatorserver.entity.seller.Seller;

import java.util.List;

public interface SellerService {

    List<SellerDTO> findAll();

    SellerDTO findBySellerId(String sellerId);

    SellerDTO save(SellerDTO sellerDTO);

    void deleteBySellerId(String sellerId);

    SellerDTO updateSeller(String sellerId, SellerDTO sellerDTO);

    void resetPassword(String username, String currentPassword, String newPassword);

    Seller findSellerByUserId(Long userId);
}
