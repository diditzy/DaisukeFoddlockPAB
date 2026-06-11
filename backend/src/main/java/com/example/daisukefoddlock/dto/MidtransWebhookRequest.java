package com.example.daisukefoddlock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MidtransWebhookRequest {
    @JsonProperty("transaction_time")
    private String transactionTime;
    
    @JsonProperty("transaction_status")
    private String transactionStatus;
    
    @JsonProperty("transaction_id")
    private String transactionId;
    
    @JsonProperty("status_message")
    private String statusMessage;
    
    @JsonProperty("status_code")
    private String statusCode;
    
    @JsonProperty("signature_key")
    private String signatureKey;
    
    @JsonProperty("payment_type")
    private String paymentType;
    
    @JsonProperty("order_id")
    private String orderId;
    
    @JsonProperty("merchant_id")
    private String merchantId;
    
    @JsonProperty("gross_amount")
    private String grossAmount;
    
    @JsonProperty("fraud_status")
    private String fraudStatus;
    
    @JsonProperty("currency")
    private String currency;
}
