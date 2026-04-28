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

    public boolean checkEmailExists(String email) {
        return sellerCoordinatorRepository.existsByEmail(email) || coordinatorRepository.existsByEmail(email);
    }

    public boolean checkPhoneExists(String mobile) {
        return sellerCoordinatorRepository.existsByMobile(mobile) || coordinatorRepository.existsByMobile(mobile);
    }

    public boolean checkDocumentExists(String documentnumber) {
        return sellerDocumentRepository.existsByDocumentNumber(documentnumber) || TempSellerDocumentRepository.existsByDocumentNumber(documentnumber);
    }

    public boolean checkGSTNumberExists(String gstnumber) {
        return sellerGSTRepository.existsByGstNumber(gstnumber) || tempSellerRepository.existsByGstNumber(gstnumber);
    }

}