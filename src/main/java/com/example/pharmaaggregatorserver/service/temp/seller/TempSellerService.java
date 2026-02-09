package com.example.pharmaaggregatorserver.service.temp.seller;

import com.example.pharmaaggregatorserver.dto.seller.TempSellerRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.TempSellerResponseDTO;

public interface TempSellerService {
    TempSellerResponseDTO createTempSeller(TempSellerRequestDTO requestDTO);
}
