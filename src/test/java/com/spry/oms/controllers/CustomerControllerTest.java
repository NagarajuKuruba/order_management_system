package com.spry.oms.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spry.oms.dtos.CustomerRequest;
import com.spry.oms.dtos.CustomerResponse;
import com.spry.oms.dtos.PaginationResponse;
import com.spry.oms.exceptions.ConflictException;
import com.spry.oms.exceptions.ResourceNotFoundException;
import com.spry.oms.services.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for CustomerController using MockMvc
 */
@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CustomerController Tests")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerRequest customerRequest;
    private CustomerResponse customerResponse;

    @BeforeEach
    void setUp() {

        customerRequest = CustomerRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .address("123 Main St")
                .build();

        customerResponse = CustomerResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .address("123 Main St")
                .build();
    }

    @Test
    @DisplayName("Should create customer successfully")
    void testCreateCustomerSuccess() throws Exception {

        when(customerService.create(any(CustomerRequest.class)))
                .thenReturn(customerResponse);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));

        verify(customerService, times(1))
                .create(any(CustomerRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when creating customer with invalid email")
    void testCreateCustomerInvalidEmail() throws Exception {

        CustomerRequest invalidRequest = CustomerRequest.builder()
                .name("John Doe")
                .email("invalid-email")
                .address("123 Main St")
                .build();

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all customers successfully")
    void testGetAllCustomersSuccess() throws Exception {

        PaginationResponse<CustomerResponse> paginationResponse =
                PaginationResponse.<CustomerResponse>builder()
                        .content(List.of(customerResponse))
                        .page(0)
                        .size(10)
                        .totalElements(1)
                        .totalPages(1)
                        .last(true)
                        .build();

        when(customerService.getAll(0, 10))
                .thenReturn(paginationResponse);

        mockMvc.perform(get("/api/customers?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("John Doe"));

        verify(customerService, times(1))
                .getAll(0, 10);
    }

    @Test
    @DisplayName("Should get customer by id successfully")
    void testGetCustomerByIdSuccess() throws Exception {

        when(customerService.getById(1L))
                .thenReturn(customerResponse);

        mockMvc.perform(get("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("John Doe"));

        verify(customerService, times(1))
                .getById(1L);
    }

    @Test
    @DisplayName("Should return 404 when customer not found")
    void testGetCustomerByIdNotFound() throws Exception {

        when(customerService.getById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Customer",
                        "ID",
                        999L
                ));

        mockMvc.perform(get("/api/customers/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update customer successfully")
    void testUpdateCustomerSuccess() throws Exception {

        CustomerRequest updateRequest = CustomerRequest.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .address("456 Oak St")
                .build();

        CustomerResponse updatedResponse = CustomerResponse.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane@example.com")
                .address("456 Oak St")
                .build();

        when(customerService.update(eq(1L), any(CustomerRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Jane Doe"))
                .andExpect(jsonPath("$.data.email").value("jane@example.com"));

        verify(customerService, times(1))
                .update(eq(1L), any(CustomerRequest.class));
    }

    @Test
    @DisplayName("Should return 409 when email already exists")
    void testUpdateCustomerEmailConflict() throws Exception {

        CustomerRequest updateRequest = CustomerRequest.builder()
                .name("John Doe")
                .email("existing@example.com")
                .address("123 Main St")
                .build();

        when(customerService.update(eq(1L), any(CustomerRequest.class)))
                .thenThrow(new ConflictException(
                        "Email already exists",
                        "EMAIL_CONFLICT"
                ));

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should delete customer successfully")
    void testDeleteCustomerSuccess() throws Exception {

        doNothing().when(customerService).delete(1L);

        mockMvc.perform(delete("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(customerService, times(1))
                .delete(1L);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent customer")
    void testDeleteCustomerNotFound() throws Exception {

        doThrow(new ResourceNotFoundException("Customer", "ID", 999L))
                .when(customerService)
                .delete(999L);

        mockMvc.perform(delete("/api/customers/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}