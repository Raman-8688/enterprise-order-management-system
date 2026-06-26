package com.enterprise.oms.product.service.impl;

import com.enterprise.oms.product.client.InventoryClient;
import com.enterprise.oms.product.dto.InventoryResponse;
import com.enterprise.oms.product.dto.request.CreateProductRequest;
import com.enterprise.oms.product.dto.request.UpdateProductRequest;
import com.enterprise.oms.product.dto.response.ProductResponse;
import com.enterprise.oms.product.dto.response.ProductStockStatusResponse;
import com.enterprise.oms.product.exception.DuplicateSkuException;
import com.enterprise.oms.product.exception.ProductNotFoundException;
import com.enterprise.oms.product.model.Product;
import com.enterprise.oms.product.repository.ProductRepository;
import com.enterprise.oms.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;

    @Override
    @Transactional
    @CacheEvict(value = {"products", "productBySku"}, allEntries = true)
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating new product with SKU: {}", request.getSku());

        // Check for duplicate SKU
        if (productRepository.existsBySku(request.getSku())) {
            log.error("Product with SKU {} already exists", request.getSku());
            throw new DuplicateSkuException(request.getSku());
        }

        // Convert DTO to Entity
        Product product = new Product();
        BeanUtils.copyProperties(request, product);
        product.setActive(true);

        // Save to database
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {} and SKU: {}", savedProduct.getId(), savedProduct.getSku());

        return convertToResponse(savedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "productBySku"}, allEntries = true)
    public ProductResponse updateProduct(String id, UpdateProductRequest request) {
        log.info("Updating product with ID: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        // Update fields
        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setCategory(request.getCategory());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setWeight(request.getWeight());
        existingProduct.setDimensions(request.getDimensions());
        existingProduct.setUpdatedBy(request.getUpdatedBy());

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully with ID: {}", updatedProduct.getId());

        return convertToResponse(updatedProduct);
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(String id) {
        log.debug("Fetching product by ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        return convertToResponse(product);
    }

    @Override
    @Cacheable(value = "productBySku", key = "#sku")
    public ProductResponse getProductBySku(String sku) {
        log.debug("Fetching product by SKU: {}", sku);

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));

        return convertToResponse(product);
    }

    @Override
    public List<ProductResponse> getAllActiveProducts() {
        log.debug("Fetching all active products");

        List<Product> products = productRepository.findByActiveTrue();
        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductResponse> getAllActiveProducts(Pageable pageable) {
        log.debug("Fetching all active products with pagination");

        Page<Product> productPage = productRepository.findByActiveTrue(pageable);
        return productPage.map(this::convertToResponse);
    }

    @Override
    public List<ProductResponse> getProductsByCategory(String category) {
        log.debug("Fetching products by category: {}", category);

        List<Product> products = productRepository.findByCategory(category);
        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Fetching products by price range: {} - {}", minPrice, maxPrice);

        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> searchProductsByName(String name) {
        log.debug("Searching products by name: {}", name);

        List<Product> products = productRepository.searchByName(name);
        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProduct(String id) {
        log.info("Deleting product with ID: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with ID: " + id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "productBySku"}, allEntries = true)
    public void deactivateProduct(String sku) {
        log.info("Deactivating product with SKU: {}", sku);

        if (!productRepository.existsBySku(sku)) {
            throw new ProductNotFoundException("Product not found with SKU: " + sku);
        }

        productRepository.deactivateBySku(sku);
        log.info("Product deactivated successfully with SKU: {}", sku);
    }

    @Override
    public ProductStockStatusResponse getProductStockStatus(String sku) {
        log.info("Getting stock status for SKU: {}", sku);

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));

        ProductStockStatusResponse.ProductStockStatusResponseBuilder responseBuilder = ProductStockStatusResponse.builder()
                .sku(sku)
                .productName(product.getName())
                .productActive(product.getActive());

        try {
            // Call Inventory Service via Feign Client
            InventoryResponse inventoryResponse = inventoryClient.checkStock(sku);

            if (inventoryResponse != null) {
                responseBuilder.inventoryAvailable(inventoryResponse.getAvailable() != null && inventoryResponse.getAvailable())
                        .availableQuantity(inventoryResponse.getAvailableQuantity() != null ? inventoryResponse.getAvailableQuantity() : 0)
                        .message(inventoryResponse.getMessage() != null ? inventoryResponse.getMessage() : "Stock available")
                        .circuitBreakerOpen(false);
            } else {
                responseBuilder.inventoryAvailable(false)
                        .availableQuantity(0)
                        .message("Inventory service returned null response")
                        .circuitBreakerOpen(true);
            }

            log.info("Inventory status for SKU {}: available={}, quantity={}",
                    sku,
                    inventoryResponse != null ? inventoryResponse.getAvailable() : null,
                    inventoryResponse != null ? inventoryResponse.getAvailableQuantity() : 0);

        } catch (Exception e) {
            log.error("Failed to fetch inventory status for SKU: {}", sku, e);
            responseBuilder.inventoryAvailable(false)
                    .availableQuantity(0)
                    .message("Inventory service temporarily unavailable. Please try again later.")
                    .circuitBreakerOpen(true);
        }

        return responseBuilder.build();
    }

    @Override
    public boolean productExists(String sku) {
        log.debug("Checking if product exists with SKU: {}", sku);
        return productRepository.existsBySku(sku);
    }

    @Override
    public long getActiveProductsCount() {
        log.debug("Getting active products count");
        return productRepository.countActiveProducts();
    }

    // Private helper methods
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