package com.enterprise.oms.product.controller;

import com.enterprise.oms.product.dto.request.CreateProductRequest;
import com.enterprise.oms.product.dto.request.UpdateProductRequest;
import com.enterprise.oms.product.dto.response.ProductResponse;
import com.enterprise.oms.product.dto.response.ProductStockStatusResponse;
import com.enterprise.oms.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("REST request to create product with SKU: {}", request.getSku());
        ProductResponse response = productService.createProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllActiveProducts() {
        log.info("REST request to get all active products");
        List<ProductResponse> products = productService.getAllActiveProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<ProductResponse>> getAllActiveProductsPaged(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to get all active products with pagination");
        Page<ProductResponse> products = productService.getAllActiveProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        log.info("REST request to get product by ID: {}", id);
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        log.info("REST request to get product by SKU: {}", sku);
        ProductResponse product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable String id,
                                                         @Valid @RequestBody UpdateProductRequest request) {
        log.info("REST request to update product with ID: {}", id);
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        log.info("REST request to delete product with ID: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{sku}/deactivate")
    public ResponseEntity<Void> deactivateProduct(@PathVariable String sku) {
        log.info("REST request to deactivate product with SKU: {}", sku);
        productService.deactivateProduct(sku);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable String category) {
        log.info("REST request to get products by category: {}", category);
        List<ProductResponse> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<ProductResponse>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        log.info("REST request to get products by price range: {} - {}", minPrice, maxPrice);
        List<ProductResponse> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String name) {
        log.info("REST request to search products by name: {}", name);
        List<ProductResponse> products = productService.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{sku}/stock-status")
    public ResponseEntity<ProductStockStatusResponse> getProductStockStatus(@PathVariable String sku) {
        log.info("REST request to get stock status for SKU: {}", sku);
        ProductStockStatusResponse response = productService.getProductStockStatus(sku);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists/{sku}")
    public ResponseEntity<Boolean> checkProductExists(@PathVariable String sku) {
        log.info("REST request to check if product exists with SKU: {}", sku);
        boolean exists = productService.productExists(sku);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/count/active")
    public ResponseEntity<Long> getActiveProductsCount() {
        log.info("REST request to get active products count");
        long count = productService.getActiveProductsCount();
        return ResponseEntity.ok(count);
    }
}