package com.enterprise.oms.product.controller;

import com.enterprise.oms.product.dto.request.CreateProductRequest;
import com.enterprise.oms.product.dto.response.ProductResponse;
import com.enterprise.oms.product.dto.response.ProductStockStatusResponse;
import com.enterprise.oms.product.model.Product;
import com.enterprise.oms.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    // FIXED: Return Page instead of List
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        log.info("REST request to get all products with pagination");
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productService.getAllProducts(pageable, search);
        Page<ProductResponse> responsePage = productPage.map(this::convertToResponse);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProductResponse>> getActiveProducts() {
        log.info("REST request to get all active products");
        List<Product> products = productService.getActiveProducts();
        return ResponseEntity.ok(products.stream().map(this::convertToResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        log.info("REST request to get product by ID: {}", id);
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(convertToResponse(product));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        log.info("REST request to get product by SKU: {}", sku);
        Product product = productService.getProductBySku(sku);
        return ResponseEntity.ok(convertToResponse(product));
    }

    @GetMapping("/{sku}/stock-status")
    public ResponseEntity<ProductStockStatusResponse> getStockStatus(@PathVariable String sku) {
        log.info("REST request to get stock status for SKU: {}", sku);
        ProductStockStatusResponse response = productService.getStockStatus(sku);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("REST request to create product with SKU: {}", request.getSku());
        Product product = productService.createProduct(request);
        return new ResponseEntity<>(convertToResponse(product), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable String id, @Valid @RequestBody CreateProductRequest request) {
        log.info("REST request to update product: {}", id);
        Product product = productService.updateProduct(id, request);
        return ResponseEntity.ok(convertToResponse(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        log.info("REST request to delete product: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String query) {
        log.info("REST request to search products: {}", query);
        List<Product> products = productService.searchProducts(query);
        return ResponseEntity.ok(products.stream().map(this::convertToResponse).toList());
    }

    private ProductResponse convertToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .brand(product.getBrand())
                .weight(product.getWeight())
                .dimensions(product.getDimensions())
                .active(product.getActive())
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}