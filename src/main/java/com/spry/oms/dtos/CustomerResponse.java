package com.spry.oms.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class CustomerResponse {
    private Long id;
    private String name;
    private String email;
    private String address;
}
