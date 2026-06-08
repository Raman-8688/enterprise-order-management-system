package com.enterprise.oms.inventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockRequest {

    @NotNull(message = "Quantity change is required")
    @PositiveOrZero(message = "Quantity change must be positive or zero")
    private Integer quantityChange;

    @NotBlank(message = "Operation type is required")
    @Pattern(regexp = "ADD|REMOVE|SET", message = "Operation must be ADD, REMOVE, or SET")
    private String operation;

    @Size(max = 100, message = "Updated by cannot exceed 100 characters")
    private String updatedBy;

    private String notes;
}