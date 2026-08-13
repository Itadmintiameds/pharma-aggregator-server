package com.example.pharmaaggregatorserver.service.temp.buyer;

import com.example.pharmaaggregatorserver.repository.temp.buyer.TempBuyerContactRepository;
import com.example.pharmaaggregatorserver.repository.temp.buyer.TempBuyerDocumentRepository;
import com.example.pharmaaggregatorserver.repository.temp.buyer.TempBuyerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Existence checks used by the buyer registration wizard. Mirrors
 * service.temp.seller.TempSellerCoordinatorService.
 */
@Service
public class TempBuyerContactService {

    @Autowired
    private TempBuyerContactRepository contactRepository;
    @Autowired
    private TempBuyerDocumentRepository tempBuyerDocumentRepository;
    @Autowired
    private TempBuyerRepository tempBuyerRepository;

    // excludeTempBuyerId lets a buyer re-visit the Contact step of their own
    // in-progress draft without the email they already saved there being
    // reported back to them as a duplicate.
    public boolean checkEmailExists(String email, Long excludeTempBuyerId) {
        return excludeTempBuyerId != null
                ? contactRepository.existsByEmailAndBuyer_TempBuyerIdNot(email, excludeTempBuyerId)
                : contactRepository.existsByEmail(email);
    }

    public boolean checkPhoneExists(String mobile, Long excludeTempBuyerId) {
        return excludeTempBuyerId != null
                ? contactRepository.existsByMobileAndBuyer_TempBuyerIdNot(mobile, excludeTempBuyerId)
                : contactRepository.existsByMobile(mobile);
    }

    public boolean checkGstNumberExists(String gstNumber) {
        return tempBuyerRepository.existsByGstNumber(gstNumber);
    }

    public boolean checkPanNumberExists(String panNumber) {
        return tempBuyerRepository.existsByPanNumber(panNumber);
    }

    public boolean checkDocumentExists(String documentNumber) {
        return tempBuyerDocumentRepository.existsByDocumentNumber(documentNumber);
    }
}
