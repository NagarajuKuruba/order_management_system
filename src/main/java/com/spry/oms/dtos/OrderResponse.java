package com.spry.oms.dtos;

import com.spry.oms.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
public class OrderResponse {
    private Long id;
    private String customerName;
    private List<OrderItemResponse> items;
    private OrderStatus status;
    private BigDecimal totalValue;
    private Integer version; // For optimistic locking
}
