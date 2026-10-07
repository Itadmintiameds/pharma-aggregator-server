package com.example.pharmaaggregatorserver.service.product.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductImportStrategyFactory {

    /**
     * Spring injects all ProductImportStrategy beans keyed by their bean name.
     * Convention: bean name must EXACTLY match the sheet name in the Excel file
     * (case-insensitive lookup handled below).
     *
     * To add a new category:
     *   1. Create a class implementing ProductImportStrategy
     *   2. Annotate it with @Component("YOUR_SHEET_NAME")
     *   3. No other changes needed here.
     */
    private final Map<String, ProductImportStrategy> strategies;

    public ProductImportStrategy getStrategy(String categoryName) {
        // Normalise: uppercase, trim — bean names like "DRUGS", "CONSUMABLE"
        String key = categoryName.trim().toUpperCase();

        return strategies.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(key))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No import strategy registered for category: " + categoryName
                                + ". Registered strategies: " + strategies.keySet()
                ));
    }
}
