package com.enterprise.oms.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory", uniqueConstraints = {
        @UniqueConstraint(name = "uk_sku", columnNames = "sku")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    @Column(name = "minimum_stock_level")
    private Integer minimumStockLevel = 5;  //  Default value added

    @Column(name = "maximum_stock_level")
    private Integer maximumStockLevel = 1000;  // Default value added

    @Column(name = "reorder_point")
    private Integer reorderPoint = 10;  //  Default value added

    @Column(name = "location", length = 100)
    private String location = "WAREHOUSE_MAIN";

    @Column(name = "supplier_info", length = 500)
    private String supplierInfo;

    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;

    @Column(name = "last_restocked_by", length = 100)
    private String lastRestockedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    // Helper method to get available quantity (total - reserved)
    public Integer getAvailableQuantity() {
        return (this.quantity != null ? this.quantity : 0) -
                (this.reservedQuantity != null ? this.reservedQuantity : 0);
    }

    // Helper method to check if stock is sufficient
    public Boolean hasSufficientStock(Integer requestedQuantity) {
        return getAvailableQuantity() >= (requestedQuantity != null ? requestedQuantity : 0);
    }

    // Helper method to check if need to reorder
    public Boolean needsReorder() {
        Integer available = getAvailableQuantity();
        Integer reorder = this.reorderPoint != null ? this.reorderPoint : 10;
        return available <= reorder;
    }
}