package com.spry.oms.services;
import com.spry.oms.dtos.*;
import com.spry.oms.entities.Customer;
import com.spry.oms.entities.Order;
import com.spry.oms.enums.OrderStatus;
import com.spry.oms.exceptions.*;
import com.spry.oms.mappers.OrderItemMapper;
import com.spry.oms.mappers.OrderMapper;
import com.spry.oms.repos.CustomerRepository;
import com.spry.oms.repos.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RabbitTemplate rabbitTemplate;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    /**
     * Validates order request
     */
    private void validateOrderRequest(OrderRequest request) {
        log.debug("Validating order request");

        if (request.getCustomerId() == null) {
            log.error("Order request validation failed: Customer ID is null");
            throw new ValidationException("customerId", "Customer ID cannot be null");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            log.error("Order request validation failed: Items list is empty");
            throw new ValidationException("items", "Order must contain at least one item");
        }

        for (OrderItemRequest item : request.getItems()) {
            if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Order validation failed: Invalid price for item: {}", item.getProductName());
                throw new ValidationException("items", "Item price must be greater than zero");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                log.error("Order validation failed: Invalid quantity for item: {}", item.getProductName());
                throw new ValidationException("items", "Item quantity must be greater than zero");
            }
        }

        log.debug("Order request validation passed");
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for customer ID: {}", request.getCustomerId());
        log.debug("Order request details: {}", request);

        try {
            // Validate request
            validateOrderRequest(request);

            // Check if customer exists
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> {
                        log.error("Customer not found with ID: {}", request.getCustomerId());
                        return new ResourceNotFoundException("Customer", "ID", request.getCustomerId());
                    });

            log.debug("Customer found: {}", customer.getId());
            Order order = orderMapper.toEntity(request);
            log.debug("Order entity mapped");

            order.setStatus(OrderStatus.PENDING);
            order.setCustomer(customer);

            // Set parent reference
            order.getItems()
                    .forEach(item -> item.setOrder(order));

            log.debug("Number of order items: {}", order.getItems().size());

            Order savedOrder = orderRepository.save(order);
            log.info("Order saved successfully with ID: {}", savedOrder.getId());

            // Publish message to RabbitMQ
            try {
                log.debug("Publishing order message to RabbitMQ - Exchange: {}, RoutingKey: {}", exchange, routingKey);
                rabbitTemplate.convertAndSend(
                        exchange,
                        routingKey,
                        savedOrder.getId()
                );
                log.info("Order message published to RabbitMQ for order ID: {}", savedOrder.getId());
            } catch (Exception e) {
                log.error("Failed to publish order message to RabbitMQ for order ID: {}", savedOrder.getId(), e);
                throw new MessagingException("Order publishing", "Failed to publish order to message queue", e);
            }

            OrderResponse response = orderMapper.toResponse(savedOrder);
            return response;
        } catch (ApplicationException e) {
            log.error("Application exception creating order for customer ID: {}", request.getCustomerId(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating order for customer ID: {}", request.getCustomerId(), e);
            throw new DataAccessException("Create order", "Failed to create order", e);
        }
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatusUpdateRequest request) {
        log.info("Updating order status - Order ID: {}, New Status: {}", orderId, request.getStatus());

        try {

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("Order not found with ID: {}", orderId);
                        return new ResourceNotFoundException("Order", "ID", orderId);
                    });

            log.debug("Order found with current status: {}", order.getStatus());

            // Business rule validation - cannot transition backwards in the order lifecycle
            boolean isValidTransition = isValidStatusTransition(order.getStatus(), request.getStatus());
            if (!isValidTransition) {
                log.warn("Invalid status transition - Order ID: {} - From: {} To: {}", 
                        orderId, order.getStatus(), request.getStatus());
                throw new InvalidOperationException(
                        String.format("Invalid status transition from %s to %s. Main flow: PENDING → CONFIRMED → SHIPPED → DELIVERED. " +
                                "Order can be CANCELLED from any state.", 
                                order.getStatus(), request.getStatus()),
                        "INVALID_STATUS_TRANSITION");
            }

            log.debug("Status transition valid - From: {} To: {}", order.getStatus(), request.getStatus());
            order.setVersion(request.getVersion());
            order.setStatus(request.getStatus());
            Order orderRes = orderRepository.save(order);
            log.info("Order status updated successfully - Order ID: {}, New Status: {}", orderId, request.getStatus());

            return orderMapper.toResponse(orderRes);
        } catch (ApplicationException e) {
            log.error("Application exception updating order status - Order ID: {}, Status: {}", orderId, request.getStatus(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating order status - Order ID: {}, Status: {}", orderId, request.getStatus(), e);
            throw new DataAccessException("Update order status", "Failed to update order status", e);
        }
    }

    @Transactional
    public BigDecimal getTotal(Long orderId) {
        log.info("Fetching total value for order ID: {}", orderId);

        try {
            if (orderId == null) {
                log.error("Order ID is null");
                throw new ValidationException("orderId", "Order ID cannot be null");
            }

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("Order not found with ID: {}", orderId);
                        return new ResourceNotFoundException("Order", "ID", orderId);
                    });

            log.debug("Order found with status: {}", order.getStatus());

            // Business rule: Total can only be retrieved for confirmed or shipped orders
            if (order.getStatus() != OrderStatus.CONFIRMED
                    && order.getStatus() != OrderStatus.SHIPPED) {
                log.warn("Attempt to get total for non-finalized order - Order ID: {}, Status: {}", orderId, order.getStatus());
                throw new BusinessRuleViolationException(
                        "Order must be in CONFIRMED or SHIPPED status to retrieve total",
                        "ORDER_NOT_FINALIZED");
            }

            BigDecimal total = order.getTotalValue();
            log.info("Total value fetched for order ID: {} = {}", orderId, total);
            return total;
        } catch (ApplicationException e) {
            log.error("Application exception fetching total for order ID: {}", orderId, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching total for order ID: {}", orderId, e);
            throw new DataAccessException("Get order total", "Failed to fetch order total", e);
        }
    }

    public OrderResponse getOrderById(Long orderId) {
        log.info("Fetching order by ID: {}", orderId);

        try {
            if (orderId == null || orderId <= 0) {
                log.error("Invalid order ID: {}", orderId);
                throw new ValidationException("orderId", "Order ID must be greater than zero");
            }

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("Order not found with ID: {}", orderId);
                        return new ResourceNotFoundException("Order", "ID", orderId);
                    });

            log.debug("Order found - ID: {}, Status: {}", orderId, order.getStatus());
            log.info("Order fetched successfully - ID: {}", orderId);

            return orderMapper.toResponse(order);
        } catch (ApplicationException e) {
            log.error("Application exception fetching order ID: {}", orderId, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching order ID: {}", orderId, e);
            throw new DataAccessException("Get order", "Failed to fetch order", e);
        }
    }

    public PaginationResponse<OrderResponse> getOrders(
            Long customerId,
            int page,
            int size) {

        log.info("Fetching orders for customer ID: {} - Page: {}, Size: {}", customerId, page, size);

        try {

            // Verify customer exists
            if (!customerRepository.existsById(customerId)) {
                log.error("Customer not found with ID: {}", customerId);
                throw new ResourceNotFoundException("Customer", "ID", customerId);
            }

            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by("id").descending()
            );

            log.debug("Created pageable: {}", pageable);
            Page<Order> orderPage = orderRepository.findByCustomerId(customerId, pageable);

            log.info("Found {} orders for customer ID: {}", orderPage.getNumberOfElements(), customerId);

            List<OrderResponse> responses = orderPage.getContent()
                    .stream()
                    .map(orderMapper::toResponse)
                    .toList();

            PaginationResponse<OrderResponse> paginationResponse = PaginationResponse.<OrderResponse>builder()
                    .content(responses)
                    .page(orderPage.getNumber())
                    .size(orderPage.getSize())
                    .totalElements(orderPage.getTotalElements())
                    .totalPages(orderPage.getTotalPages())
                    .last(orderPage.isLast())
                    .build();

            log.debug("Pagination response prepared - Total elements: {}", paginationResponse.getTotalElements());
            return paginationResponse;
        } catch (ApplicationException e) {
            log.error("Application exception fetching orders for customer ID: {}", customerId, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching orders for customer ID: {}", customerId, e);
            throw new DataAccessException("Get orders", "Failed to fetch customer orders", e);
        }
    }

    /**
     * Cancel an order by transitioning its status to CANCELLED.
     * An order can be cancelled from any state in the lifecycle.
     *
     * @param orderId the order ID to cancel
     */
    @Transactional
    public void deleteOrder(Long orderId) {
        log.info("Cancelling order ID: {}", orderId);

        try {
            if (orderId == null || orderId <= 0) {
                log.error("Invalid order ID for cancellation: {}", orderId);
                throw new ValidationException("orderId", "Order ID must be greater than zero");
            }

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("Order not found with ID: {}", orderId);
                        return new ResourceNotFoundException("Order", "ID", orderId);
                    });

            log.debug("Order found for cancellation - ID: {}, Current Status: {}", orderId, order.getStatus());

            // Prevent cancelling an already cancelled order
            if (order.getStatus() == OrderStatus.CANCELLED) {
                log.warn("Order is already cancelled - Order ID: {}", orderId);
                throw new BusinessRuleViolationException(
                        "Order is already in CANCELLED status",
                        "ORDER_ALREADY_CANCELLED");
            }

            // Transition to CANCELLED status
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("Order cancelled successfully - Order ID: {}, Previous Status: {}", orderId, order.getStatus());
        } catch (ApplicationException e) {
            log.error("Application exception cancelling order ID: {}", orderId, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error cancelling order ID: {}", orderId, e);
            throw new DataAccessException("Cancel order", "Failed to cancel order", e);
        }
    }

    /**
     * Validates if a status transition is allowed.
     * Main flow: PENDING → CONFIRMED → SHIPPED → DELIVERED
     * Special rule: Can transition to CANCELLED from ANY state at any time.
     *
     * @param currentStatus the current order status
     * @param newStatus the new status to transition to
     * @return true if transition is valid, false otherwise
     */
    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Same status is not a transition
        if (currentStatus == newStatus) {
            log.debug("No status change - same status: {}", currentStatus);
            return false;
        }

        // Can always transition to CANCELLED from any state
        if (newStatus == OrderStatus.CANCELLED) {
            log.debug("Valid transition to CANCELLED from current status: {}", currentStatus);
            return true;
        }

        // Cannot transition FROM cancelled to any other state
        if (currentStatus == OrderStatus.CANCELLED) {
            log.debug("Cannot transition from CANCELLED status to any other status");
            return false;
        }

        // Define valid forward transitions (only forward in main flow)
        return (currentStatus == OrderStatus.PENDING && newStatus == OrderStatus.CONFIRMED) ||
               (currentStatus == OrderStatus.CONFIRMED && newStatus == OrderStatus.SHIPPED) ||
               (currentStatus == OrderStatus.SHIPPED && newStatus == OrderStatus.DELIVERED);
    }
}