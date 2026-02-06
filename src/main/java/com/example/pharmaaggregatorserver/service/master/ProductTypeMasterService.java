package com.example.pharmaaggregatorserver.service.master;

import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;

import java.util.List;

public interface ProductTypeMasterService {

    // Get all product types
    public List<ProductTypeMaster> getAllProductTypes();
}