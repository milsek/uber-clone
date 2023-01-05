package com.example.springbackend.util;

public enum PayPalEndpoints {
    GET_ACCESS_TOKEN("/v1/oauth2/token"),
    GET_CLIENT_TOKEN("/v1/identity/generate-token"),
    ORDER_CHECKOUT("/v2/checkout/orders");

    private final String path;

    PayPalEndpoints(String path) {
        this.path = path;
    }

    public static String createUrl(String baseUrl, PayPalEndpoints endpoint) {
        return baseUrl + endpoint.path;
    }

    public static String createUrl(String baseUrl, PayPalEndpoints endpoint, String... params) {
        return baseUrl + String.format(endpoint.path, params);
    }

}
