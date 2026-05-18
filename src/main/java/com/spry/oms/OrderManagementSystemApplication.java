package com.spry.oms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class OrderManagementSystemApplication {

	public static void main(String[] args) {
		log.info("Starting Order Management System Application...");
		try {
			SpringApplication.run(OrderManagementSystemApplication.class, args);
			log.info("Order Management System Application started successfully");
		} catch (Exception e) {
			log.error("Failed to start Order Management System Application", e);
			throw e;
		}
	}

}
