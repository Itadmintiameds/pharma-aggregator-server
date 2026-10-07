package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.ProductCategoryMasterDto;
import com.example.pharmaaggregatorserver.dto.product.ProductSubcategoryMasterDto;
import com.example.pharmaaggregatorserver.mapper.product.ProductCategoryMapper;
import com.example.pharmaaggregatorserver.repository.product.ProductCategoryMasterRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductSubcategoryMasterRepository;
import com.example.pharmaaggregatorserver.service.product.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryMasterRepository productCategoryMasterRepository;
    private final ProductSubcategoryMasterRepository productSubcategoryMasterRepository;
    private final ProductCategoryMapper productCategoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryMasterDto> getProductCategoriesByCategoryId(Long categoryId) {

        return productCategoryMasterRepository.findByCategory_CategoryId(categoryId)
                .stream()
                .map(productCategoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSubcategoryMasterDto> getByProductCategoryId(Long productCategoryId) {

        return productSubcategoryMasterRepository.findByProductCategoryMaster_ProductCategoryId(productCategoryId)
                .stream()
                .map(productCategoryMapper::toSubcategoryDto)
                .toList();
    }
}