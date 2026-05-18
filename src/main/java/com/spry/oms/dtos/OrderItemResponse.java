package com.spry.oms.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    private Long id;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
}
