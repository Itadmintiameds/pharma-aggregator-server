package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.entity.product.ProductAttributeDrug;
import com.example.pharmaaggregatorserver.entity.product.ProductAttributeFoodInfant;
import com.example.pharmaaggregatorserver.entity.product.ProductUserManual;
import com.example.pharmaaggregatorserver.repository.product.ProductAttributeDrugRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductAttributeFoodInfantRepository;
import com.example.pharmaaggregatorserver.repository.product.ProductUserManualRepository;
import com.example.pharmaaggregatorserver.service.S3Service;
import com.example.pharmaaggregatorserver.service.product.ProductUserManualService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductUserManualServiceImpl implements ProductUserManualService {

    private final ProductAttributeDrugRepository drugRepository;
    private final ProductUserManualRepository manualRepository;
    private final ProductAttributeFoodInfantRepository productAttributeFoodInfantRepository;
    private final S3Service s3Service;

    @Override
    public String uploadManual(String productAttributeId, MultipartFile file) {

        ProductAttributeDrug drug = drugRepository.findById(productAttributeId)
                .orElseThrow(() -> new RuntimeException("Product attribute not found"));

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String key = "products/usermanual/" + productAttributeId + "/"
                + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        String fileUrl = s3Service.uploadFile(key, file);

        ProductUserManual manual = manualRepository
                .findByProductAttributeDrug_ProductAttributeId(productAttributeId)
                .orElse(new ProductUserManual());

        manual.setUserManualUrl(fileUrl);
        manual.setProductAttributeDrug(drug);

        drug.setProductUserManual(manual);

        manualRepository.save(manual);
        return fileUrl;
    }


    @Override
    public String uploadUserManual(String productAttributeId, MultipartFile file) {

        ProductAttributeFoodInfant entity = productAttributeFoodInfantRepository.findById(productAttributeId)
                .orElseThrow(() ->
                        new RuntimeException("Food Infant Product Attribute not found"));

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String key = "productUserManual/"
                + productAttributeId + "/"
                + System.currentTimeMillis() + "_"
                + file.getOriginalFilename();

        String fileUrl = s3Service.uploadFile(key, file);

        entity.setProductUserManual(fileUrl);

        productAttributeFoodInfantRepository.save(entity);

        return fileUrl;
    }

}
