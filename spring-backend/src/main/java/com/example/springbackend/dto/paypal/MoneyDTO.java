package com.example.springbackend.dto.paypal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoneyDTO {
    @JsonProperty("currency_code")
    private String currencyCode;
    private String value;
}
