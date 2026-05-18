package com.spry.oms.repos;

import com.spry.oms.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

	// Check for existing customer by email (used to prevent duplicates)
	boolean existsByEmail(String email);

}