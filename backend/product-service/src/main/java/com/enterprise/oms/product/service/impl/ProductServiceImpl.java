package com.enterprise.oms.product.service.impl;

import com.enterprise.oms.product.client.InventoryClient;
import com.enterprise.oms.product.dto.request.CreateProductRequest;
import com.enterprise.oms.product.dto.request.UpdateProductRequest;
import com.enterprise.oms.product.dto.response.InventoryResponse;
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
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException(request.getSku());
        }

        Product product = new Product();
        BeanUtils.copyProperties(request, product);
        product.setActive(true);

        Product savedProduct = productRepository.save(product);
        return convertToResponse(savedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "productBySku"}, allEntries = true)
    public ProductResponse updateProduct(String id, UpdateProductRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setCategory(request.getCategory());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setWeight(request.getWeight());
        existingProduct.setDimensions(request.getDimensions());
        existingProduct.setUpdatedBy(request.getUpdatedBy());

        Product updatedProduct = productRepository.save(existingProduct);
        return convertToResponse(updatedProduct);
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
        return convertToResponse(product);
    }

    @Override
    @Cacheable(value = "productBySku", key = "#sku")
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
        return convertToResponse(product);
    }

    @Override
    public List<ProductResponse> getAllActiveProducts() {
        List<Product> products = productRepository.findByActiveTrue();
        return products.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public Page<ProductResponse> getAllActiveProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findByActiveTrue(pageable);
        return productPage.map(this::convertToResponse);
    }

    @Override
    public List<ProductResponse> getProductsByCategory(String category) {
        List<Product> products = productRepository.findByCategory(category);
        return products.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return products.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> searchProductsByName(String name) {
        List<Product> products = productRepository.searchByName(name);
        return products.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "productBySku"}, allEntries = true)
    public void deactivateProduct(String sku) {
        if (!productRepository.existsBySku(sku)) {
            throw new ProductNotFoundException("Product not found with SKU: " + sku);
        }
        productRepository.deactivateBySku(sku);
    }

    @Override
    public ProductStockStatusResponse getProductStockStatus(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));

        ProductStockStatusResponse.ProductStockStatusResponseBuilder responseBuilder =
                ProductStockStatusResponse.builder()
                        .sku(sku)
                        .productName(product.getName())
                        .productActive(product.getActive());

        try {
            InventoryResponse inventoryResponse = inventoryClient.checkStock(sku);
            if (inventoryResponse != null) {
                responseBuilder
                        .inventoryAvailable(Boolean.TRUE.equals(inventoryResponse.getAvailable()))
                        .availableQuantity(inventoryResponse.getAvailableQuantity() != null ? inventoryResponse.getAvailableQuantity() : 0)
                        .message(inventoryResponse.getMessage())
                        .circuitBreakerOpen(false);
            } else {
                responseBuilder
                        .inventoryAvailable(false)
                        .availableQuantity(0)
                        .message("Inventory service unavailable")
                        .circuitBreakerOpen(true);
            }
        } catch (Exception e) {
            responseBuilder
                    .inventoryAvailable(false)
                    .availableQuantity(0)
                    .message("Inventory service error")
                    .circuitBreakerOpen(true);
        }

        return responseBuilder.build();
    }

    @Override
    public boolean productExists(String sku) {
        return productRepository.existsBySku(sku);
    }

    @Override
    public long getActiveProductsCount() {
        return productRepository.countActiveProducts();
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