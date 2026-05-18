package com.spry.oms.mappers;

import com.spry.oms.dtos.OrderRequest;
import com.spry.oms.dtos.OrderResponse;
import com.spry.oms.entities.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "customer.name", target = "customerName")
    OrderResponse toResponse(Order order);

    Order toEntity(OrderRequest dto);
}