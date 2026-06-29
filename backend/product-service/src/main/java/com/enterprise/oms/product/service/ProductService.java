package com.enterprise.oms.product.service;

import com.enterprise.oms.product.dto.request.CreateProductRequest;
import com.enterprise.oms.product.dto.response.ProductResponse;
import com.enterprise.oms.product.dto.response.ProductStockStatusResponse;
import com.enterprise.oms.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Page<Product> getAllProducts(Pageable pageable, String search);
    List<Product> getActiveProducts();
    Product getProductById(String id);
    Product getProductBySku(String sku);
    ProductStockStatusResponse getStockStatus(String sku);
    Product createProduct(CreateProductRequest request);
    Product updateProduct(String id, CreateProductRequest request);
    void deleteProduct(String id);
    List<Product> searchProducts(String query);
}