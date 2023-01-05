package com.example.springbackend.dto.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClientTokenDTO {
    @JsonProperty("client_token")
    private String clientToken;
    @JsonProperty("expires_in")
    private Long expiresIn;
}
