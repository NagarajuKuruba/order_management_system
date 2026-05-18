package com.spry.oms.controllers;

import com.spry.oms.dtos.*;
import com.spry.oms.enums.OrderStatus;
import com.spry.oms.exceptions.ValidationException;
import com.spry.oms.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Tag(name = "Orders", description = "Order Management APIs")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create Order")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @Valid @RequestBody OrderRequest request) {

        log.info("Creating order for customer ID: {}", request.getCustomerId());
        try {
            OrderResponse response =
                    orderService.createOrder(request);

            log.info("Order created successfully with ID: {}", response.getId());
            ApiResponse<OrderResponse> apiResponse =
                    ApiResponse.<OrderResponse>builder()
                            .success(true)
                            .message("Order created successfully")
                            .data(response)
                            .timestamp(LocalDateTime.now())
                            .build();

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(apiResponse);
        } catch (Exception e) {
            log.error("Error creating order for customer ID: {}", request.getCustomerId(), e);
            throw e;
        }
    }

    @Operation(summary = "Get Order by ID")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long orderId) {

        log.info("Fetching order by ID: {}", orderId);
        try {
            if (orderId == null || orderId <= 0) {
                log.warn("Invalid order ID: {}", orderId);
                throw new ValidationException("orderId", "Order ID must be greater than zero");
            }

            OrderResponse response = orderService.getOrderById(orderId);

            log.info("Order fetched by id: {}", orderId);
            ApiResponse<OrderResponse> apiResponse =
                    ApiResponse.<OrderResponse>builder()
                            .success(true)
                            .message("Order fetched successfully")
                            .data(response)
                            .timestamp(LocalDateTime.now())
                            .build();

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error fetching order ID: {}", orderId, e);
            throw e;
        }
    }

    @Operation(summary = "Get Orders by Customer")
    @GetMapping("/customer/{customerId}")
    public ApiResponse<PaginationResponse<OrderResponse>> getOrders(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        log.info("Fetching orders for customer ID: {} with page: {}, size: {}", customerId, page, size);
        try {
            // Additional validation
            if (customerId == null || customerId <= 0) {
                log.warn("Invalid customer ID: {}", customerId);
                throw new ValidationException("customerId", "Customer ID must be greater than zero");
            }

            if (page < 0) {
                log.warn("Invalid page number: {}", page);
                throw new ValidationException("page", "Page number cannot be negative");
            }

            if (size <= 0 || size > 100) {
                log.warn("Invalid page size: {}", size);
                throw new ValidationException("size", "Page size must be between 1 and 100");
            }

            PaginationResponse<OrderResponse> data = orderService.getOrders(customerId, page, size);
            log.info("Orders fetched successfully for customer ID: {}. Total orders: {}", customerId, data.getTotalElements());
            return ApiResponse.<PaginationResponse<OrderResponse>>builder()
                    .success(true)
                    .message("Orders fetched successfully")
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error fetching orders for customer ID: {}", customerId, e);
            throw e;
        }
    }

    @Operation(summary = "Update Order Status")
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long orderId, @RequestBody OrderStatusUpdateRequest request) {

        log.info("Updating order ID: {} status to: {} with version: {}", orderId, request.getStatus(), request.getVersion());
        try {
            // Additional validation
            if (orderId == null || orderId <= 0) {
                log.warn("Invalid order ID: {}", orderId);
                throw new ValidationException("orderId", "Order ID must be greater than zero");
            }

            if (request.getStatus() == null) {
                log.warn("Status is null for order ID: {}", orderId);
                throw new ValidationException("status", "Status cannot be null");
            }

            OrderResponse response =
                    orderService.updateStatus(orderId, request);

            log.info("Order ID: {} status updated to: {} successfully", orderId, request.getStatus());
            ApiResponse<OrderResponse> apiResponse =
                    ApiResponse.<OrderResponse>builder()
                            .success(true)
                            .message("Order status updated successfully")
                            .data(response)
                            .timestamp(LocalDateTime.now())
                            .build();

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error updating order ID: {} status to: {}", orderId, request.getStatus(), e);
            throw e;
        }
    }

    @Operation(summary = "Get Total Value of Order")
    @GetMapping("/{orderId}/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotal(
            @PathVariable Long orderId) {

        log.info("Fetching total value for order ID: {}", orderId);
        try {
            // Additional validation
            if (orderId == null || orderId <= 0) {
                log.warn("Invalid order ID: {}", orderId);
                throw new ValidationException("orderId", "Order ID must be greater than zero");
            }

            BigDecimal total =
                    orderService.getTotal(orderId);

            log.info("Total value fetched for order ID: {} = {}", orderId, total);
            ApiResponse<BigDecimal> apiResponse =
                    ApiResponse.<BigDecimal>builder()
                            .success(true)
                            .message("Order total fetched successfully")
                            .data(total)
                            .timestamp(LocalDateTime.now())
                            .build();

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error fetching total value for order ID: {}", orderId, e);
            throw e;
        }
    }

    @Operation(summary = "Cancel Order (can be done at any state)")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long orderId) {

        log.info("Cancelling order ID: {}", orderId);
        try {
            // Additional validation
            if (orderId == null || orderId <= 0) {
                log.warn("Invalid order ID: {}", orderId);
                throw new ValidationException("orderId", "Order ID must be greater than zero");
            }

            orderService.deleteOrder(orderId);

            log.info("Order ID: {} cancelled successfully", orderId);
            ApiResponse<Void> apiResponse =
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("Order cancelled successfully")
                            .timestamp(LocalDateTime.now())
                            .build();

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error cancelling order ID: {}", orderId, e);
            throw e;
        }
    }

}
