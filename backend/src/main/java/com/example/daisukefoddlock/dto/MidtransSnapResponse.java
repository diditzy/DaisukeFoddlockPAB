package com.example.daisukefoddlock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MidtransSnapResponse {
    private String token;
    
    @JsonProperty("redirect_url")
    private String redirectUrl;
}
