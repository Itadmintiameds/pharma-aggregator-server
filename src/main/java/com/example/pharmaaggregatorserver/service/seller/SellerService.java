package com.example.pharmaaggregatorserver.service.seller;

import com.example.pharmaaggregatorserver.dto.seller.SellerDTO;

import java.util.List;

public interface SellerService {

    List<SellerDTO> findAll();

    SellerDTO findBySellerId(String sellerId);

    SellerDTO save(SellerDTO sellerDTO);

    SellerDTO deleteBySellerId(String sellerId);

    SellerDTO updateBySellerId(String sellerId, SellerDTO sellerDTO);
}
