package com.example.daisukefoddlock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MidtransSnapRequest {
    @JsonProperty("transaction_details")
    private TransactionDetails transactionDetails;

    @JsonProperty("credit_card")
    @Builder.Default
    private CreditCard creditCard = CreditCard.builder().secure(true).build();

    @JsonProperty("customer_details")
    private CustomerDetails customerDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDetails {
        @JsonProperty("order_id")
        private String orderId;

        @JsonProperty("gross_amount")
        private Integer grossAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreditCard {
        private boolean secure;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerDetails {
        private String email;
    }
}
