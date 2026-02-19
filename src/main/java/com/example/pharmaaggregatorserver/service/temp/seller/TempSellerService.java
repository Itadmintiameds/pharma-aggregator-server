package com.example.pharmaaggregatorserver.service.temp.seller;

import com.example.pharmaaggregatorserver.dto.admin.TempSellerAdminResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.TempSellerRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.TempSellerResponseDTO;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSeller;

import java.util.List;

public interface TempSellerService {
    TempSellerResponseDTO createTempSeller(TempSellerRequestDTO requestDTO);

    /* Get All Temporary Sellers */
    List<TempSellerAdminResponseDTO> getALLTempSellers();

    /* Get Temporary Sellers By Id*/
    TempSeller findById(Long id);

    void updateGstVerification(Long tempSellerId, boolean isGstVerified);
    void updateDocumentVerification(Long tempSellerId, Long documentId, boolean isDocumentVerified);
    void updateBankDocumentVerification(Long tempSellerId, boolean isBankDocumentVerified);

    void deleteTempSeller(Long tempSellerId);

    void deleteBothSellerAndTempSeller(Long tempSellerId);
}
