package com.spry.oms.worker;

import com.spry.oms.entities.Order;
import com.spry.oms.enums.OrderStatus;
import com.spry.oms.exceptions.DataAccessException;
import com.spry.oms.exceptions.ResourceNotFoundException;
import com.spry.oms.exceptions.ValidationException;
import com.spry.oms.repos.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderWorker {

    private final OrderRepository orderRepository;

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    @Transactional
    public void process(Long orderId) {
        log.info("Processing order message received - Order ID: {}", orderId);
        long startTime = System.currentTimeMillis();

        try {
            // Validate orderId
            if (orderId == null || orderId <= 0) {
                log.error("Invalid order ID received: {}", orderId);
                throw new ValidationException("orderId", "Invalid order ID");
            }

            log.debug("Fetching order from database - Order ID: {}", orderId);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("Order not found for processing - Order ID: {}", orderId);
                        return new ResourceNotFoundException("Order", "ID", orderId);
                    });

            log.debug("Order found - Current Status: {}, Items Count: {}", 
                    order.getStatus(), order.getItems().size());

            // Validate order items
            if (order.getItems() == null || order.getItems().isEmpty()) {
                log.error("Order has no items - Order ID: {}", orderId);
                throw new ValidationException("items", "Order must have at least one item");
            }

            // Calculate total value
            BigDecimal total = order.getItems()
                    .stream()
                    .map(item -> {
                        if (item.getPrice() == null || item.getQuantity() == null) {
                            log.error("Invalid item data - Order ID: {}, Item ID: {}", orderId, item.getId());
                            throw new ValidationException("items", "Item price or quantity is null");
                        }
                        BigDecimal itemTotal = item.getPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity()));
                        log.debug("Calculated item total - Item: {}, Price: {}, Quantity: {}, Total: {}", 
                                item.getId(), item.getPrice(), item.getQuantity(), itemTotal);
                        return itemTotal;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.info("Total value calculated for order ID: {} = {}", orderId, total);

            if (total.compareTo(BigDecimal.ZERO) < 0) {
                log.error("Calculated negative total for order ID: {}", orderId);
                throw new com.spry.oms.exceptions.BusinessRuleViolationException(
                        "Order total cannot be negative");
            }

            order.setTotalValue(total);
            order.setStatus(OrderStatus.CONFIRMED);
            log.debug("Order status updated to CONFIRMED");

            orderRepository.save(order);
            log.info("Order confirmed and saved - Order ID: {}", orderId);

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Order processing completed successfully - Order ID: {}, Processing Time: {}ms", 
                    orderId, processingTime);
        } catch (com.spry.oms.exceptions.ApplicationException e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Application exception processing order - Order ID: {}, Processing Time: {}ms, Error: {}", 
                    orderId, processingTime, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Unexpected error processing order - Order ID: {}, Processing Time: {}ms", 
                    orderId, processingTime, e);
            throw new DataAccessException("Process order", "Failed to process order", e);
        }
    }
}