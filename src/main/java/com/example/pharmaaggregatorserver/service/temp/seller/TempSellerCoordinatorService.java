package com.example.pharmaaggregatorserver.service.temp.seller;

import com.example.pharmaaggregatorserver.entity.temp.seller.TempSellerCoordinator;
import com.example.pharmaaggregatorserver.repository.seller.SellerCoordinatorRepository;
import com.example.pharmaaggregatorserver.repository.seller.SellerDocumentRepository;
import com.example.pharmaaggregatorserver.repository.seller.SellerGSTRepository;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerCoordinatorRepository;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerDocumentRepository;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TempSellerCoordinatorService {

    @Autowired
    private TempSellerCoordinatorRepository coordinatorRepository;
    @Autowired
    private SellerCoordinatorRepository sellerCoordinatorRepository;
    @Autowired
    private SellerDocumentRepository sellerDocumentRepository;
    @Autowired
    private TempSellerDocumentRepository TempSellerDocumentRepository;
    @Autowired
    private SellerGSTRepository sellerGSTRepository;
    @Autowired
    private TempSellerRepository tempSellerRepository;

    //Seller Registration
    // excludeTempSellerId lets a seller re-visit the Coordinator step of
    // their own in-progress draft without the email they already saved
    // there on an earlier "Continue" (auto-save) being reported back to
    // them as a duplicate.
    public boolean checkEmailExists(String email, Long excludeTempSellerId) {
        boolean existsInTempDrafts = excludeTempSellerId != null
                ? coordinatorRepository.existsByEmailAndSeller_TempSellerIdNot(email, excludeTempSellerId)
                : coordinatorRepository.existsByEmail(email);
        return sellerCoordinatorRepository.existsByEmail(email) || existsInTempDrafts;
    }
    //Seller Registration
    public boolean checkPhoneExists(String mobile, Long excludeTempSellerId) {
        boolean existsInTempDrafts = excludeTempSellerId != null
                ? coordinatorRepository.existsByMobileAndSeller_TempSellerIdNot(mobile, excludeTempSellerId)
                : coordinatorRepository.existsByMobile(mobile);
        return sellerCoordinatorRepository.existsByMobile(mobile) || existsInTempDrafts;
    }
    //Seller Registration
    public boolean checkDocumentExists(String documentnumber) {
        return sellerDocumentRepository.existsByDocumentNumber(documentnumber) || TempSellerDocumentRepository.existsByDocumentNumber(documentnumber);
    }
    //Seller Registration
    public boolean checkGSTNumberExists(String gstnumber) {
        return sellerGSTRepository.existsByGstNumber(gstnumber) || tempSellerRepository.existsByGstNumber(gstnumber);
    }

    //Seller profile Update
    public boolean checkProfileEmailExists(String email) {
        return sellerCoordinatorRepository.existsByEmail(email);
    }
    //Seller profile Update
    public boolean checkProfilePhoneExists(String mobile) {
        return sellerCoordinatorRepository.existsByMobile(mobile);
    }
    //Seller profile Update
    public boolean checkProfileDocumentExists(String documentnumber) {
        return sellerDocumentRepository.existsByDocumentNumber(documentnumber);
    }
    //Seller profile Update
    public boolean checkProfileGSTNumberExists(String gstnumber) {
        return sellerGSTRepository.existsByGstNumber(gstnumber);
    }


}