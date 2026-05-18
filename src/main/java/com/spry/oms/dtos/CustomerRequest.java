package com.spry.oms.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CustomerRequest {

    @NotBlank(message = "Customer name cannot be empty")
    @Size(min = 1, max = 255, message = "Customer name must be between 1 and 255 characters")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 500, message = "Customer address cannot exceed 500 characters")
    private String address;
}