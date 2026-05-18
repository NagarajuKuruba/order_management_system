package com.spry.oms.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spry.oms.dtos.OrderItemRequest;
import com.spry.oms.dtos.OrderRequest;
import com.spry.oms.dtos.OrderResponse;
import com.spry.oms.dtos.OrderStatusUpdateRequest;
import com.spry.oms.enums.OrderStatus;
import com.spry.oms.exceptions.InvalidOperationException;
import com.spry.oms.exceptions.ResourceNotFoundException;
import com.spry.oms.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for OrderController using MockMvc
 */
@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("OrderController Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderRequest orderRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productName("Laptop")
                .price(new BigDecimal("999.99"))
                .quantity(1)
                .build();

        orderRequest = OrderRequest.builder()
                .customerId(1L)
                .items(List.of(itemRequest))
                .orderDate(LocalDateTime.now())
                .build();

        orderResponse = OrderResponse.builder()
                .id(1L)
                .customerName("John Doe")
                .status(OrderStatus.PENDING)
                .totalValue(new BigDecimal("999.99"))
                .build();
    }

    @Test
    @DisplayName("Should create order successfully")
    void testCreateOrderSuccess() throws Exception {

        when(orderService.createOrder(any(OrderRequest.class)))
                .thenReturn(orderResponse);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        verify(orderService, times(1))
                .createOrder(any(OrderRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when creating order with invalid data")
    void testCreateOrderInvalidData() throws Exception {

        OrderRequest invalidRequest = OrderRequest.builder()
                .customerId(1L)
                .items(List.of())
                .orderDate(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get order by id successfully")
    void testGetOrderByIdSuccess() throws Exception {

        when(orderService.getOrderById(1L))
                .thenReturn(orderResponse);

        mockMvc.perform(get("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.customerName").value("John Doe"));

        verify(orderService, times(1))
                .getOrderById(1L);
    }

    @Test
    @DisplayName("Should return 404 when order not found")
    void testGetOrderByIdNotFound() throws Exception {

        when(orderService.getOrderById(999L))
                .thenThrow(new ResourceNotFoundException("Order", "ID", 999L));

        mockMvc.perform(get("/api/orders/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update order status successfully")
    void testUpdateOrderStatusSuccess() throws Exception {

        OrderResponse updatedResponse = OrderResponse.builder()
                .id(1L)
                .customerName("John Doe")
                .status(OrderStatus.CONFIRMED)
                .totalValue(new BigDecimal("999.99"))
                .build();

        OrderStatusUpdateRequest statusRequest =
                OrderStatusUpdateRequest.builder()
                        .status(OrderStatus.CONFIRMED)
                        .version(1)
                        .build();

        when(orderService.updateStatus(eq(1L), any(OrderStatusUpdateRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("Should return 400 for invalid status transition")
    void testUpdateOrderStatusInvalidTransition() throws Exception {

        OrderStatusUpdateRequest statusRequest =
                OrderStatusUpdateRequest.builder()
                        .status(OrderStatus.PENDING)
                        .version(1)
                        .build();

        when(orderService.updateStatus(eq(1L), any(OrderStatusUpdateRequest.class)))
                .thenThrow(new InvalidOperationException(
                        "Invalid transition",
                        "INVALID_STATUS_TRANSITION"
                ));

        mockMvc.perform(patch("/api/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should delete order successfully")
    void testDeleteOrderSuccess() throws Exception {

        doNothing().when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(orderService, times(1))
                .deleteOrder(1L);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent order")
    void testDeleteOrderNotFound() throws Exception {

        doThrow(new ResourceNotFoundException("Order", "ID", 999L))
                .when(orderService)
                .deleteOrder(999L);

        mockMvc.perform(delete("/api/orders/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}