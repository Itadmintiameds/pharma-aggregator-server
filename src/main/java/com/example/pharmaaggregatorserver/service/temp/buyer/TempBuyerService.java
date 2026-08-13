package com.example.pharmaaggregatorserver.service.temp.buyer;

import com.example.pharmaaggregatorserver.dto.admin.TempBuyerAdminResponseDTO;
import com.example.pharmaaggregatorserver.dto.buyer.TempBuyerResponseDTO;
import com.example.pharmaaggregatorserver.dto.temp.buyer.TempBuyerDraftRequestDTO;
import com.example.pharmaaggregatorserver.dto.temp.buyer.TempBuyerRequestDTO;
import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyer;

import java.util.List;
import java.util.Optional;

public interface TempBuyerService {

    TempBuyerResponseDTO createTempBuyer(TempBuyerRequestDTO requestDTO);

    List<TempBuyerAdminResponseDTO> getAllTempBuyers();

    TempBuyer findById(Long id);

    Optional<TempBuyer> findByUserId(Long buyerUserId);

    void updateGstVerification(Long tempBuyerId, boolean isGstVerified);

    void updatePanVerification(Long tempBuyerId, boolean isPanVerified);

    void updateDocumentVerification(Long tempBuyerId, Long documentId, boolean isDocumentVerified);

    void deleteTempBuyer(Long tempBuyerId);

    void deleteBothBuyerAndTempBuyer(Long tempBuyerId);

    TempBuyerResponseDTO updateTempBuyer(Long tempBuyerId, TempBuyerRequestDTO requestDTO);

    /**
     * Create (tempBuyerId == null) or update (tempBuyerId != null) a DRAFT
     * registration from a partial, fully-optional request body.
     */
    TempBuyerResponseDTO saveDraft(Long tempBuyerId, TempBuyerDraftRequestDTO dto);

    /**
     * Promotes a DRAFT registration to a fully-submitted one, running the
     * same full validation createTempBuyer uses, then flips status to
     * SUBMITTED.
     */
    TempBuyerResponseDTO finalizeDraft(Long tempBuyerId, TempBuyerRequestDTO requestDTO);
}
