package com.example.springbackend.dto.paypal;

public enum OrderStatus {
    CREATED,
    SAVED,
    APPROVED,
    VOIDED,
    COMPLETED,
    PAYER_ACTION_REQUIRED;
}
