package com.spry.oms.dtos;

import com.spry.oms.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OrderStatusUpdateRequest {
    @NotNull(message = "Order status cannot be null")
    private OrderStatus status;

    @NotNull(message = "Version cannot be null")
    private Integer version;
}
