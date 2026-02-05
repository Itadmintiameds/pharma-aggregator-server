package com.example.pharmaaggregatorserver.service;

import com.example.pharmaaggregatorserver.entity.ProductTypeMaster;
import com.example.pharmaaggregatorserver.repository.ProductTypeMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductTypeMasterService {

    private final ProductTypeMasterRepository productTypeMasterRepository;

    // Get all product types
    public List<ProductTypeMaster> getAllProductTypes() {
        return productTypeMasterRepository.findAll();
    }
}