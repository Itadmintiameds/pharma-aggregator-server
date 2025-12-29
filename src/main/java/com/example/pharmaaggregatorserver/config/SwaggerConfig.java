package com.example.pharmaaggregatorserver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI pharmaAggregatorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pharma Aggregator Server API")
                        .description("API documentation for Pharma Aggregator Server")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Pharma Aggregator Team")
                                .email("support@pharmaaggregator.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}

