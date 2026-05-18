package com.spry.oms.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class OrderRequest {

    @NotNull(message = "Customer ID cannot be null")
    private Long customerId;

    @NotNull(message = "Order date cannot be null")
    private LocalDateTime orderDate;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "Order status cannot be null")
    private int version; // For optimistic locking
}
