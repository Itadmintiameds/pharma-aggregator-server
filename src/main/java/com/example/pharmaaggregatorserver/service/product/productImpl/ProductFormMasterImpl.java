package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.AgeGroupMasterDto;
import com.example.pharmaaggregatorserver.dto.product.ProductFormMasterDto;
import com.example.pharmaaggregatorserver.mapper.product.AgeGroupMapper;
import com.example.pharmaaggregatorserver.mapper.product.ProductFormMapper;
import com.example.pharmaaggregatorserver.repository.product.AgeGroupMasterRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductFormMasterRepository;
import com.example.pharmaaggregatorserver.service.product.ProductFormMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFormMasterImpl implements ProductFormMasterService {

    private final ProductFormMasterRepository productFormMasterRepository;
    private final ProductFormMapper productFormMapper;

    @Override
    @Transactional
    public List<ProductFormMasterDto> getAllProductForms() {
        return productFormMasterRepository.findAll()
                .stream()
                .map(productFormMapper::toDto)
                .toList();
    }
}
