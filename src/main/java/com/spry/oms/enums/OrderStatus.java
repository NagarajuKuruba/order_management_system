package com.spry.oms.enums;

/**
 * Order lifecycle states.
 * Main flow: PENDING → CONFIRMED → SHIPPED → DELIVERED
 * At any point, an order can transition to CANCELLED status.
 */
public enum OrderStatus {
    PENDING,      // Order created but not yet confirmed
    CONFIRMED,    // Order confirmed and ready to ship
    SHIPPED,      // Order is in transit
    DELIVERED,    // Order delivered to customer
    CANCELLED     // Order cancelled (can transition from any state)
}
