package com.example.pharmaaggregatorserver.service.serviceImpl.master;

import com.example.pharmaaggregatorserver.entity.master.SellerTypeMaster;
import com.example.pharmaaggregatorserver.repository.temp.master.SellerTypeMasterRepository;
import com.example.pharmaaggregatorserver.service.master.SellerTypeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerTypeMasterServiceImpl implements SellerTypeMasterService {

    private final SellerTypeMasterRepository sellerTypeMasterRepository;

    // Get all seller types
    @Override
    public List<SellerTypeMaster> getAllSellerTypes() {
        return sellerTypeMasterRepository.findAll();
    }
}