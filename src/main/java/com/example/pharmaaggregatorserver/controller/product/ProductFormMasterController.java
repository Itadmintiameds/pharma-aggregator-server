package com.example.pharmaaggregatorserver.controller.product;

import com.example.pharmaaggregatorserver.dto.product.AgeGroupMasterDto;
import com.example.pharmaaggregatorserver.dto.product.ProductFormMasterDto;
import com.example.pharmaaggregatorserver.service.product.AgeGroupService;
import com.example.pharmaaggregatorserver.service.product.ProductFormMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/productForm")
@RequiredArgsConstructor
public class ProductFormMasterController {

    private final ProductFormMasterService productFormMasterService;

    @GetMapping("/getAll")
    public List<ProductFormMasterDto> getAllProductForms() {

        return productFormMasterService.getAllProductForms();
    }

}
