package com.example.pharmaaggregatorserver.service.temp.seller;

import com.example.pharmaaggregatorserver.dto.admin.TempSellerAdminResponseDTO;
import com.example.pharmaaggregatorserver.dto.seller.TempSellerDraftRequestDTO;
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

    /**
     * Create (tempSellerId == null) or update (tempSellerId != null) a
     * DRAFT registration from a partial, fully-optional request body.
     */
    TempSellerResponseDTO saveDraft(Long tempSellerId, TempSellerDraftRequestDTO dto);

    /**
     * Promotes a DRAFT registration to a fully-submitted one, running the
     * same full validation createTempSeller uses, then flips status to OPEN.
     */
    TempSellerResponseDTO finalizeDraft(Long tempSellerId, TempSellerRequestDTO requestDTO);
}
