package com.spry.oms.services;

import com.spry.oms.dtos.CustomerRequest;
import com.spry.oms.dtos.CustomerResponse;
import com.spry.oms.dtos.PaginationResponse;
import com.spry.oms.entities.Customer;
import com.spry.oms.exceptions.DataAccessException;
import com.spry.oms.exceptions.ValidationException;
import com.spry.oms.exceptions.ResourceNotFoundException;
import com.spry.oms.exceptions.ConflictException;
import com.spry.oms.mappers.CustomerMapper;
import com.spry.oms.repos.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerMapper customerMapper;

    private final CustomerRepository customerRepository;

    public CustomerResponse create(CustomerRequest request) {
        log.debug("Creating customer with details: {}", request);
        
        try {

            if (customerRepository.existsByEmail(request.getEmail())) {
                log.error("Conflict: Email already in use: {}", request.getEmail());
                throw new ConflictException("Email already in use");
            }

            Customer customer = customerMapper.toEntity(request);
            log.debug("Mapped customer entity: {}", customer);

            Customer savedCustomer = customerRepository.save(customer);
            log.info("Customer saved successfully with ID: {}", savedCustomer.getId());

            CustomerResponse response = customerMapper.toResponse(savedCustomer);
            log.debug("Customer response created: {}", response);
            return response;
        } catch (ConflictException e) {
            log.error("Validation error creating customer: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error creating customer with request: {}", request, e);
            throw new DataAccessException("Create customer", "Failed to create customer", e);
        }
    }

    public PaginationResponse<CustomerResponse> getAll(
            int page,
            int size) {

        log.debug("Fetching customers with pagination - page: {}, size: {}", page, size);
        
        try {

            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by("id").descending()
            );
            log.debug("Created pageable: {}", pageable);

            Page<CustomerResponse> responsePage =
                    customerRepository.findAll(pageable)
                            .map(customerMapper::toResponse);

            log.info("Fetched {} customers from page {}", responsePage.getNumberOfElements(), page);

            PaginationResponse<CustomerResponse> paginationResponse = PaginationResponse.<CustomerResponse>builder()
                    .content(responsePage.getContent())
                    .page(responsePage.getNumber())
                    .size(responsePage.getSize())
                    .totalElements(responsePage.getTotalElements())
                    .totalPages(responsePage.getTotalPages())
                    .last(responsePage.isLast())
                    .build();

            log.debug("Pagination response prepared with total elements: {}", paginationResponse.getTotalElements());
            return paginationResponse;
        } catch (ValidationException e) {
            log.error("Validation error fetching customers: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error fetching customers - page: {}, size: {}", page, size, e);
            throw new DataAccessException("Get customers", "Failed to fetch customers", e);
        }
    }

    public CustomerResponse getById(Long id) {
        log.info("Fetching customer by ID: {}", id);
        try {
            if (id == null || id <= 0) {
                log.error("Invalid customer ID: {}", id);
                throw new ValidationException("id", "Customer ID must be a positive number");
            }

            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "ID", id));

            CustomerResponse response = customerMapper.toResponse(customer);
            log.debug("Customer fetched: {}", response);
            return response;
        } catch (ValidationException | ResourceNotFoundException e) {
            log.error("Application error fetching customer by ID: {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching customer by ID: {}", id, e);
            throw new DataAccessException("Get customer", "Failed to fetch customer", e);
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        log.info("Updating customer ID: {} with data: {}", id, request);
        try {
            if (id == null || id <= 0) {
                log.error("Invalid customer ID for update: {}", id);
                throw new ValidationException("id", "Customer ID must be a positive number");
            }

            Customer existing = customerRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "ID", id));

            // If email changed, ensure uniqueness
            if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(existing.getEmail())) {
                if (customerRepository.existsByEmail(request.getEmail())) {
                    log.error("Conflict: Email already in use: {}", request.getEmail());
                    throw new ConflictException("Email already in use");
                }
            }

            // Apply updates
            existing.setName(request.getName());
            existing.setEmail(request.getEmail());
            existing.setAddress(request.getAddress());

            Customer saved = customerRepository.save(existing);
            log.info("Customer updated successfully - ID: {}", saved.getId());

            return customerMapper.toResponse(saved);
        } catch (ValidationException | ResourceNotFoundException | ConflictException e) {
            log.error("Application error updating customer ID {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating customer ID: {}", id, e);
            throw new DataAccessException("Update customer", "Failed to update customer", e);
        }
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting customer by ID: {}", id);
        try {
            if (id == null || id <= 0) {
                log.error("Invalid customer ID for delete: {}", id);
                throw new ValidationException("id", "Customer ID must be a positive number");
            }
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "ID", id));
            customer.setActive(false);
            customerRepository.save(customer);
            log.info("Customer deleted successfully - ID: {}", id);
        } catch (ValidationException | ResourceNotFoundException e) {
            log.error("Application error deleting customer ID {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting customer ID: {}", id, e);
            throw new DataAccessException("Delete customer", "Failed to delete customer", e);
        }
    }
}
