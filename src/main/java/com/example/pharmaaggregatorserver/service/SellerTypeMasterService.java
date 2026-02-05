package com.example.pharmaaggregatorserver.service;

import com.example.pharmaaggregatorserver.entity.SellerTypeMaster;
import com.example.pharmaaggregatorserver.repository.SellerTypeMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerTypeMasterService {

    private final SellerTypeMasterRepository sellerTypeMasterRepository;

    // Get all seller types
    public List<SellerTypeMaster> getAllSellerTypes() {
        return sellerTypeMasterRepository.findAll();
    }
}