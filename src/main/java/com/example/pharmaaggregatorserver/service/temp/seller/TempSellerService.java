package com.example.pharmaaggregatorserver.service.temp.seller;

import com.example.pharmaaggregatorserver.dto.admin.TempSellerAdminResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.TempSellerRequestDTO;
import com.example.pharmaaggregatorserver.dto.seller.TempSellerResponseDTO;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSeller;

import java.util.List;
import java.util.Optional;

public interface TempSellerService {
    TempSellerResponseDTO createTempSeller(TempSellerRequestDTO requestDTO);

    /* Get All Temporary Sellers */
    List<TempSellerAdminResponseDTO> getALLTempSellers();

    /* Get Temporary Sellers By Id*/
    TempSeller findById(Long id);

    /* Get Temporary Seller for the logged-in user, if their registration is still pending approval */
    Optional<TempSeller> findByUserId(Long userId);

    void updateGstVerification(Long tempSellerId, boolean isGstVerified);

    void updateDocumentVerification(Long tempSellerId, Long documentId, boolean isDocumentVerified);

    void updateBankDocumentVerification(Long tempSellerId, boolean isBankDocumentVerified);

    void updateCompanyRegistrationCertificateVerification(Long tempSellerId, boolean isCompanyRegistrationCertificateVerified);

    void deleteTempSeller(Long tempSellerId);

    void deleteBothSellerAndTempSeller(Long tempSellerId);

    TempSellerResponseDTO updateTempSeller(Long tempSellerId, TempSellerRequestDTO requestDTO);
}
