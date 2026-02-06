package com.example.pharmaaggregatorserver.service.master;

import com.example.pharmaaggregatorserver.entity.master.SellerTypeMaster;

import java.util.List;

public interface SellerTypeMasterService {
    // Get all seller types
    public List<SellerTypeMaster> getAllSellerTypes();
}