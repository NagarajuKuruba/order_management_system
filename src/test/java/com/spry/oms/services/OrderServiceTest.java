package com.spry.oms.services;

import com.spry.oms.dtos.*;
import com.spry.oms.entities.Customer;
import com.spry.oms.entities.Order;
import com.spry.oms.entities.OrderItem;
import com.spry.oms.enums.OrderStatus;
import com.spry.oms.exceptions.*;
import com.spry.oms.mappers.OrderItemMapper;
import com.spry.oms.mappers.OrderMapper;
import com.spry.oms.repos.CustomerRepository;
import com.spry.oms.repos.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService using JUnit 5 and Mockito
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderService orderService;

    private Customer testCustomer;
    private Order testOrder;
    private OrderRequest orderRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        // Set RabbitMQ config values
        ReflectionTestUtils.setField(orderService, "exchange", "test-exchange");
        ReflectionTestUtils.setField(orderService, "routingKey", "test-routing-key");

        // Initialize test data
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("John Doe");
        testCustomer.setEmail("john@example.com");
        testCustomer.setAddress("123 Main St");
        testCustomer.setCreatedAt(LocalDateTime.now());

        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductName("Laptop");
        item.setPrice(new BigDecimal("999.99"));
        item.setQuantity(1);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomer(testCustomer);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setItems(List.of(item));
        testOrder.setTotalValue(new BigDecimal("999.99"));
        testOrder.setVersion(1);
        testOrder.setCreatedAt(LocalDateTime.now());

        OrderItemRequest itemRequest = OrderItemRequest.builder().productName("Laptop").price(new BigDecimal("999.99")).quantity(1).build();


        orderRequest = OrderRequest.builder().customerId(1L).items(List.of(itemRequest)).build();


        orderResponse = OrderResponse.builder().id(1L).customerName("John Doe").status(OrderStatus.PENDING).totalValue(new BigDecimal("999.99")).build();
    }

    // ==================== CREATE ORDER TESTS ====================

    @Test
    @DisplayName("Should create order successfully with valid request")
    void testCreateOrderSuccess() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(orderMapper.toEntity(orderRequest)).thenReturn(testOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(orderResponse);

        // Act
        OrderResponse response = orderService.createOrder(orderRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getCustomerName());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when customer not found")
    void testCreateOrderCustomerNotFound() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(orderRequest));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("Should throw ValidationException when customer ID is null")
    void testCreateOrderNullCustomerId() {
        // Arrange
        OrderRequest invalidRequest = OrderRequest.builder()
                .customerId(null)
                .items(List.of(OrderItemRequest.builder().productName("Test").price(new BigDecimal("10")).quantity(1).build()))
                .build();

        // Act & Assert
        assertThrows(ValidationException.class, () -> orderService.createOrder(invalidRequest));
    }

    @Test
    @DisplayName("Should throw ValidationException when items list is empty")
    void testCreateOrderEmptyItems() {
        // Arrange
        OrderRequest invalidRequest = OrderRequest.builder().customerId(1L).items(List.of()).build();

        // Act & Assert
        assertThrows(ValidationException.class, () -> orderService.createOrder(invalidRequest));
    }

    @Test
    @DisplayName("Should throw ValidationException when item price is zero or negative")
    void testCreateOrderInvalidPrice() {
        // Arrange
        OrderItemRequest invalidItem = OrderItemRequest.builder().productName("Invalid Product").price(BigDecimal.ZERO).quantity(1).build();

        OrderRequest invalidRequest = OrderRequest.builder().customerId(1L).items(List.of(invalidItem)).build();

        // Act & Assert
        assertThrows(ValidationException.class, () -> orderService.createOrder(invalidRequest));
    }

    @Test
    @DisplayName("Should throw ValidationException when item quantity is zero or negative")
    void testCreateOrderInvalidQuantity() {
        // Arrange
        OrderItemRequest invalidItem = OrderItemRequest.builder().productName("Invalid Product").price(new BigDecimal("50.00")).quantity(0).build();

        OrderRequest invalidRequest = OrderRequest.builder().customerId(1L).items(List.of(invalidItem)).build();


        // Act & Assert
        assertThrows(ValidationException.class, () -> orderService.createOrder(invalidRequest));
    }

    @Test
    @DisplayName("Should throw MessagingException when RabbitMQ publish fails")
    void testCreateOrderRabbitMQFailure() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(orderMapper.toEntity(orderRequest)).thenReturn(testOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doThrow(new RuntimeException("RabbitMQ connection failed"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyLong());

        // Act & Assert
        assertThrows(MessagingException.class, () -> orderService.createOrder(orderRequest));
    }

    // ==================== UPDATE STATUS TESTS ====================

    @Test
    @DisplayName("Should update order status from PENDING to CONFIRMED successfully")
    void testUpdateStatusPendingToConfirmed() {
        // Arrange
        testOrder.setStatus(OrderStatus.PENDING);
        OrderStatusUpdateRequest statusRequest = OrderStatusUpdateRequest.builder().status(OrderStatus.CONFIRMED).version(1).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(orderResponse);

        // Act
        OrderResponse response = orderService.updateStatus(1L, statusRequest);

        // Assert
        assertNotNull(response);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Should update order status from CONFIRMED to SHIPPED successfully")
    void testUpdateStatusConfirmedToShipped() {
        // Arrange
        testOrder.setStatus(OrderStatus.CONFIRMED);
        OrderStatusUpdateRequest statusRequest = OrderStatusUpdateRequest.builder().status(OrderStatus.SHIPPED).version(1).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(orderResponse);

        // Act
        OrderResponse response = orderService.updateStatus(1L, statusRequest);

        // Assert
        assertNotNull(response);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Should allow transition to CANCELLED from any state")
    void testUpdateStatusToCancelledFromPending() {
        // Arrange
        testOrder.setStatus(OrderStatus.PENDING);
        OrderStatusUpdateRequest cancelRequest = OrderStatusUpdateRequest.builder().status(OrderStatus.CANCELLED).version(1).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(orderResponse);

        // Act
        OrderResponse response = orderService.updateStatus(1L, cancelRequest);

        // Assert
        assertNotNull(response);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Should throw InvalidOperationException for invalid backward transition")
    void testUpdateStatusInvalidBackwardTransition() {
        // Arrange
        testOrder.setStatus(OrderStatus.SHIPPED);
        OrderStatusUpdateRequest invalidRequest = OrderStatusUpdateRequest.builder().status(OrderStatus.CONFIRMED).version(1).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> orderService.updateStatus(1L, invalidRequest));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order not found for status update")
    void testUpdateStatusOrderNotFound() {
        // Arrange
        OrderStatusUpdateRequest statusRequest = OrderStatusUpdateRequest.builder()
                .status(OrderStatus.CONFIRMED)
                .version(1)
                .build();

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.updateStatus(999L, statusRequest));
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when transitioning from CANCELLED")
    void testUpdateStatusFromCancelled() {
        // Arrange
        testOrder.setStatus(OrderStatus.CANCELLED);
        OrderStatusUpdateRequest invalidRequest = OrderStatusUpdateRequest.builder().status(OrderStatus.CONFIRMED).version(1).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> orderService.updateStatus(1L, invalidRequest));
    }

    // ==================== GET TOTAL TESTS ====================

    @Test
    @DisplayName("Should get total for CONFIRMED order successfully")
    void testGetTotalConfirmedOrder() {
        // Arrange
        testOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        BigDecimal total = orderService.getTotal(1L);

        // Assert
        assertEquals(new BigDecimal("999.99"), total);
    }

    @Test
    @DisplayName("Should get total for SHIPPED order successfully")
    void testGetTotalShippedOrder() {
        // Arrange
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        BigDecimal total = orderService.getTotal(1L);

        // Assert
        assertEquals(new BigDecimal("999.99"), total);
    }

    @Test
    @DisplayName("Should throw BusinessRuleViolationException for PENDING order total")
    void testGetTotalPendingOrder() {
        // Arrange
        testOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () -> orderService.getTotal(1L));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order not found for total")
    void testGetTotalOrderNotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.getTotal(999L));
    }

    // ==================== GET ORDERS (PAGINATION) TESTS ====================

    @Test
    @DisplayName("Should retrieve orders with pagination successfully")
    void testGetOrdersWithPagination() {

        // Arrange
        when(customerRepository.existsById(1L)).thenReturn(true);

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by("id").descending()
        );

        Page<Order> orderPage =
                new PageImpl<>(List.of(testOrder), pageable, 1);

        when(orderRepository.findByCustomerId(1L, pageable))
                .thenReturn(orderPage);

        when(orderMapper.toResponse(testOrder))
                .thenReturn(orderResponse);

        // Act
        PaginationResponse<OrderResponse> response =
                orderService.getOrders(1L, 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    @DisplayName("Should return empty list when customer has no orders")
    void testGetOrdersEmptyResult() {

        // Arrange
        when(customerRepository.existsById(1L)).thenReturn(true);

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by("id").descending()
        );

        Page<Order> emptyPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(orderRepository.findByCustomerId(1L, pageable))
                .thenReturn(emptyPage);

        // Act
        PaginationResponse<OrderResponse> response =
                orderService.getOrders(1L, 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getContent().size());
        assertEquals(0, response.getTotalElements());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when customer not found for orders")
    void testGetOrdersCustomerNotFound() {
        // Arrange
        when(customerRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrders(999L, 0, 10));
    }

    // ==================== DELETE/CANCEL ORDER TESTS ====================

    @Test
    @DisplayName("Should cancel order successfully from PENDING state")
    void testDeleteOrderFromPending() {
        // Arrange
        testOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        orderService.deleteOrder(1L);

        // Assert
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Should cancel order successfully from CONFIRMED state")
    void testDeleteOrderFromConfirmed() {
        // Arrange
        testOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        orderService.deleteOrder(1L);

        // Assert
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Should throw BusinessRuleViolationException when order already cancelled")
    void testDeleteOrderAlreadyCancelled() {
        // Arrange
        testOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () -> orderService.deleteOrder(1L));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order not found for delete")
    void testDeleteOrderNotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrder(999L));
    }

    @Test
    @DisplayName("Should throw ValidationException for invalid order ID (null)")
    void testDeleteOrderNullId() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> orderService.deleteOrder(null));
    }

    @Test
    @DisplayName("Should throw ValidationException for invalid order ID (zero)")
    void testDeleteOrderZeroId() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> orderService.deleteOrder(0L));
    }
}

