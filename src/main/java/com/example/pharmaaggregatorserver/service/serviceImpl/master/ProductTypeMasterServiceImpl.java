package com.example.pharmaaggregatorserver.service.serviceImpl.master;

import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.ProductTypeResponseDTO;
import com.example.pharmaaggregatorserver.entity.master.ProductTypeMaster;
import com.example.pharmaaggregatorserver.repository.master.ProductTypeMasterRepository;
import com.example.pharmaaggregatorserver.service.master.ProductTypeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductTypeMasterServiceImpl implements ProductTypeMasterService {

    private final ProductTypeMasterRepository productTypeMasterRepository;

    private ProductTypeResponseDTO convertToResponseDTO(ProductTypeMaster entity) {
        return new ProductTypeResponseDTO(
                entity.getProductTypeId(),
                entity.getProductTypeName(),
                entity.getRegulatoryCategory(),
                entity.getIsActive()
        );
    }

    @Override
    public List<ProductTypeResponseDTO> getAllProductTypes() {
        return productTypeMasterRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
}