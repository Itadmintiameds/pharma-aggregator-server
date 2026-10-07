package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.entity.product.CosmeticPersonalCareMasters.ProductsFormMaster;
import com.example.pharmaaggregatorserver.entity.product.NetQuantityUnit;
import com.example.pharmaaggregatorserver.repository.product.NetQuantityUnitRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductsFormMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NetQuantityUnitService {

    private final NetQuantityUnitRepository netQuantityUnitRepository;
    private final ProductsFormMasterRepository productsFormMasterRepository;

    public List<NetQuantityUnit> getUnitsByCategoryId(Long categoryId) {
        return netQuantityUnitRepository.findByCategoryCategoryId(categoryId);
    }


    // Get all forms
    public List<ProductsFormMaster> getAllForms() {
        return productsFormMasterRepository.findAll();
    }

}
