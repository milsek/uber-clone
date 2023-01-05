
package com.example.springbackend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "paypal")
public class PaypalConfig {

    private String baseUrl;
    private String clientId;
    private String secret;
}
