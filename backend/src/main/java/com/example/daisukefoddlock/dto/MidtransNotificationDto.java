package com.example.daisukefoddlock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MidtransNotificationDto {
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("transaction_status")
    private String transactionStatus; // settlement, pending, deny, expire, cancel, etc.

    @JsonProperty("gross_amount")
    private String grossAmount;

    @JsonProperty("payment_type")
    private String paymentType;

    @JsonProperty("signature_key")
    private String signatureKey;

    @JsonProperty("status_code")
    private String statusCode;

    @JsonProperty("status_message")
    private String statusMessage;

    @JsonProperty("transaction_id")
    private String transactionId;
}
