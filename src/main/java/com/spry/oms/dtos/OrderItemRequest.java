package com.spry.oms.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class OrderItemRequest {

    @NotBlank(message = "Product name cannot be empty")
    @Size(min = 1, max = 255, message = "Product name must be between 1 and 255 characters")
    private String productName;

    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;
}