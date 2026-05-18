package com.spry.oms.controllers;

import com.spry.oms.dtos.ApiResponse;
import com.spry.oms.dtos.CustomerRequest;
import com.spry.oms.dtos.CustomerResponse;
import com.spry.oms.dtos.PaginationResponse;
import com.spry.oms.exceptions.ValidationException;
import com.spry.oms.services.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        log.info("Creating customer with name: {}", request.getName());

        CustomerResponse response = customerService.create(request);
        log.info("Customer created successfully with ID: {}", response.getId());
        return ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer created successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();

    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerResponse> getById(@PathVariable Long id) {
        log.info("GET customer by id: {}", id);

        CustomerResponse response = customerService.getById(id);
        log.info("Customer fetched by id: {}", id);
        return ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer fetched successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();

    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        log.info("Updating customer id: {}", id);
        CustomerResponse response = customerService.update(id, request);
        log.info("Customer updated id: {}", id);
        return ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer updated successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        log.info("Deleting customer id: {}", id);

            customerService.delete(id);
            log.info("Customer deleted id: {}", id);
            return ApiResponse.<String>builder()
                    .success(true)
                    .message("Customer deleted successfully")
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();
    }

    @GetMapping
    public ApiResponse<PaginationResponse<CustomerResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching customers with page: {}, size: {}", page, size);
        
        try {

            PaginationResponse<CustomerResponse> data = customerService.getAll(page, size);
            log.info("Customers fetched successfully. Total elements: {}", data.getTotalElements());
            return ApiResponse.<PaginationResponse<CustomerResponse>>builder()
                    .success(true)
                    .message("Customers fetched successfully")
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error fetching customers with page: {}, size: {}", page, size, e);
            throw e;
        }
    }
}