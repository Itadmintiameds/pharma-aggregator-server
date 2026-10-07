package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.AgeGroupMasterDto;
import com.example.pharmaaggregatorserver.dto.product.ProductFormMasterDto;

import java.util.List;

public interface ProductFormMasterService {

    List<ProductFormMasterDto> getAllProductForms();
}
