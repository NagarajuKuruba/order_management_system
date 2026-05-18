package com.spry.oms.mappers;

import com.spry.oms.dtos.OrderItemRequest;
import com.spry.oms.dtos.OrderItemResponse;
import com.spry.oms.entities.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    OrderItemResponse toResponse(OrderItem order);

    OrderItem toEntity(OrderItemRequest dto);
}