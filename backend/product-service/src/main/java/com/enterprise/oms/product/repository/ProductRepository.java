package com.enterprise.oms.product.repository;

import com.enterprise.oms.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByActiveTrue();

    Page<Product> findByActiveTrue(Pageable pageable);

    List<Product> findByCategory(String category);

    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name% AND p.active = true")
    List<Product> searchByName(@Param("name") String name);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.price <= :maxPrice")
    List<Product> findByCategoryAndMaxPrice(@Param("category") String category,
                                            @Param("maxPrice") BigDecimal maxPrice);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.active = false WHERE p.sku = :sku")
    void deactivateBySku(@Param("sku") String sku);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true")
    long countActiveProducts();
}