package com.example.pharmaaggregatorserver.service.master;

import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.ProductTypeResponseDTO;

import java.util.List;

public interface ProductTypeMasterService {
    List<ProductTypeResponseDTO> getAllProductTypes();
}