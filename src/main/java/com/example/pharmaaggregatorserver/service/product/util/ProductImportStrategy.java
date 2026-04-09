package com.example.pharmaaggregatorserver.service.product.util;

import com.example.pharmaaggregatorserver.dto.product.ProductDetailsDto;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;

public interface ProductImportStrategy {

    /**
     * Maps a single Excel row (0-indexed columns, data starts at row index 2)
     * to a ProductDetailsDto ready for productService.createProduct().
     */
    ProductDetailsDto mapRow(Row row);

    default ProductDetailsDto mapCsv(CSVRecord record) {
        throw new UnsupportedOperationException("CSV import not supported for this category");
    }
}
