package com.enterprise.oms.product.service.impl;

import com.enterprise.oms.product.client.InventoryClient;
import com.enterprise.oms.product.dto.request.CreateProductRequest;
import com.enterprise.oms.product.dto.response.ProductResponse;
import com.enterprise.oms.product.dto.response.ProductStockStatusResponse;
import com.enterprise.oms.product.exception.ProductNotFoundException;
import com.enterprise.oms.product.model.Product;
import com.enterprise.oms.product.repository.ProductRepository;
import com.enterprise.oms.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable, String search) {
        log.debug("Getting all products with pagination");
        if (search != null && !search.isEmpty()) {
            return productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(search, search, pageable);
        }
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        log.debug("Getting all active products");
        return productRepository.findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(String id) {
        log.debug("Getting product by ID: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        log.debug("Getting product by SKU: {}", sku);
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductStockStatusResponse getStockStatus(String sku) {
        log.debug("Getting stock status for SKU: {}", sku);
        Product product = getProductBySku(sku);
        ProductStockStatusResponse response = ProductStockStatusResponse.builder()
                .sku(sku)
                .productName(product.getName())
                .productActive(product.getActive())
                .inventoryAvailable(false)
                .availableQuantity(0)
                .message("Inventory service unavailable")
                .circuitBreakerOpen(false)
                .build();

        try {
            // Call inventory service
            var inventory = inventoryClient.checkStock(sku);
            if (inventory != null) {
                response.setInventoryAvailable(inventory.getAvailable());
                response.setAvailableQuantity(inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0);
                response.setMessage(inventory.getMessage() != null ? inventory.getMessage() : "Stock available");
            }
        } catch (Exception e) {
            log.warn("Inventory service error for SKU {}: {}", sku, e.getMessage());
            response.setMessage("Could not fetch inventory data");
            response.setCircuitBreakerOpen(true);
        }

        return response;
    }

    @Override
    public Product createProduct(CreateProductRequest request) {
        log.info("Creating product with SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            throw new RuntimeException("Product with SKU " + request.getSku() + " already exists");
        }

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .brand(request.getBrand())
                .weight(request.getWeight())
                .dimensions(request.getDimensions())
                .active(true)
                .createdBy(request.getCreatedBy())
                .build();

        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(String id, CreateProductRequest request) {
        log.info("Updating product: {}", id);

        Product product = getProductById(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setBrand(request.getBrand());
        product.setWeight(request.getWeight());
        product.setDimensions(request.getDimensions());
        product.setUpdatedBy(request.getCreatedBy());

        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(String id) {
        log.info("Deleting product: {}", id);
        Product product = getProductById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String query) {
        log.debug("Searching products: {}", query);
        return productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(query, query);
    }
}