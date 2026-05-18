package com.spry.oms.mappers;

import com.spry.oms.dtos.CustomerRequest;
import com.spry.oms.dtos.CustomerResponse;
import com.spry.oms.entities.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface CustomerMapper {

    CustomerResponse toResponse(Customer customer);

    Customer toEntity(CustomerRequest dto);
}