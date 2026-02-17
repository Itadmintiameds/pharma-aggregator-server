package com.example.pharmaaggregatorserver.service.temp.seller;

import com.example.pharmaaggregatorserver.dto.response.temp.seller.TempSellerAdminResponseDTO;
import com.example.pharmaaggregatorserver.dto.response.temp.seller.TempSellerResponseDTO;

import java.util.List;

public interface TempSellerService {

    /* Get All Temporary Sellers */
    public List<TempSellerAdminResponseDTO> getALLTempSellers();

    /* Get Temporary Sellers By Id*/
    public TempSellerResponseDTO findById();
}
