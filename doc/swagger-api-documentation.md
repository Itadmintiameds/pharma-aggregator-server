## Swagger API Documentation

This project uses **SpringDoc OpenAPI 3** (Swagger) for API documentation.

### Accessing Swagger UI

Once the application is running, you can access the Swagger UI at:

- **Development**: `http://localhost:8080/swagger-ui.html`
- **Test**: `http://localhost:8081/swagger-ui.html`
- **Production**: Swagger UI is **disabled** for security reasons

### API Documentation Endpoints

- **Swagger UI**: `/swagger-ui.html`
- **OpenAPI JSON**: `/api-docs` or `/v3/api-docs`
- **OpenAPI YAML**: `/v3/api-docs.yaml`

### Environment-Specific Configuration

#### Development (`dev` profile)
- Swagger UI: **Enabled**
- Try it out: **Enabled**
- Access: `http://localhost:8080/swagger-ui.html`

#### Test (`test` profile)
- Swagger UI: **Enabled**
- Try it out: **Enabled**
- Access: `http://localhost:8081/swagger-ui.html`

#### Production (`prod` profile)
- Swagger UI: **Disabled** (for security)
- API docs endpoint: Still available at `/api-docs` if needed

### Configuration Files

Swagger configuration is managed in:

- **Base config**: `src/main/resources/application.yml`
- **Dev config**: `src/main/resources/application-dev.yml`
- **Test config**: `src/main/resources/application-test.yml`
- **Prod config**: `src/main/resources/application-prod.yml`

### Security

Swagger endpoints are configured to be accessible without authentication in the `SecurityConfig` class. The following paths are permitted:

- `/swagger-ui.html`
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/api-docs/**`
- `/swagger-resources/**`
- `/webjars/**`

### Customizing API Documentation

The API information (title, description, version, contact) is configured in:

`src/main/java/com/example/pharmaaggregatorserver/config/SwaggerConfig.java`

You can customize:
- API title and description
- Version number
- Contact information
- License information

### Using Swagger Annotations

To document your API endpoints, use Swagger annotations in your controllers:

```java
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve a list of all products")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved products")
    public ResponseEntity<List<Product>> getAllProducts() {
        // Implementation
    }

    @PostMapping
    @Operation(summary = "Create a product", description = "Create a new product")
    @ApiResponse(responseCode = "201", description = "Product created successfully")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        // Implementation
    }
}
```

### Dependencies

Swagger is included via the following Maven dependency in `pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### Troubleshooting

**Swagger UI not accessible:**
1. Check that the application is running
2. Verify the correct port (8080 for dev, 8081 for test)
3. Ensure you're using the correct profile
4. Check that Swagger is enabled in the profile configuration

**Swagger UI shows "No operations available":**
- This is normal if you haven't created any REST controllers yet
- Once you add `@RestController` classes with `@RequestMapping`, they will appear in Swagger

**Security blocking Swagger:**
- Verify that `SecurityConfig` allows Swagger endpoints
- Check that the security configuration is properly loaded

