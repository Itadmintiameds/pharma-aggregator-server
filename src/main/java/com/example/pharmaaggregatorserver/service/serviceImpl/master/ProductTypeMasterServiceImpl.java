package com.example.pharmaaggregatorserver.service.serviceImpl.master;

import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import com.example.pharmaaggregatorserver.repository.temp.master.ProductTypeMasterRepository;
import com.example.pharmaaggregatorserver.service.master.ProductTypeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductTypeMasterServiceImpl implements ProductTypeMasterService {

    private final ProductTypeMasterRepository productTypeMasterRepository;

    // Get all product types
    @Override
    public List<ProductTypeMaster> getAllProductTypes() {
        return productTypeMasterRepository.findAll();
    }
}