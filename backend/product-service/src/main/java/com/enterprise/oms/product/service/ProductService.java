package com.enterprise.oms.product.service;

import com.enterprise.oms.product.dto.request.CreateProductRequest;
import com.enterprise.oms.product.dto.request.UpdateProductRequest;
import com.enterprise.oms.product.dto.response.ProductResponse;
import com.enterprise.oms.product.dto.response.ProductStockStatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(String id, UpdateProductRequest request);

    ProductResponse getProductById(String id);

    ProductResponse getProductBySku(String sku);

    List<ProductResponse> getAllActiveProducts();

    Page<ProductResponse> getAllActiveProducts(Pageable pageable);

    List<ProductResponse> getProductsByCategory(String category);

    List<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    List<ProductResponse> searchProductsByName(String name);

    void deleteProduct(String id);

    void deactivateProduct(String sku);

    ProductStockStatusResponse getProductStockStatus(String sku);

    boolean productExists(String sku);

    long getActiveProductsCount();
}