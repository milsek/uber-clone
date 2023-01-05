package com.example.springbackend.config;

import com.example.springbackend.dto.paypal.*;
import com.example.springbackend.model.Order;
import com.example.springbackend.model.Passenger;
import com.example.springbackend.repository.OrderRepository;
import com.example.springbackend.service.PassengerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.example.springbackend.util.PayPalEndpoints.*;


@Component
@Slf4j
public class PayPalService {
    private HttpClient httpClient;
    @Autowired
    private PaypalConfig paypalConfig;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PassengerService passengerService;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    public PayPalService() {
        httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    }

    public AccessTokenResponseDTO getAccessToken() throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(createUrl(paypalConfig.getBaseUrl(), GET_ACCESS_TOKEN)))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, encodeBasicCredentials())
                .header(HttpHeaders.ACCEPT_LANGUAGE, "en_US")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        var content = response.body();
        return objectMapper.readValue(content, AccessTokenResponseDTO.class);
    }

    public ClientTokenDTO getClientToken() throws Exception {
        var accessTokenDto = getAccessToken();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(createUrl(paypalConfig.getBaseUrl(), GET_CLIENT_TOKEN)))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenDto.getAccessToken())
                .header(HttpHeaders.ACCEPT_LANGUAGE, "en_US")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        var content = response.body();

        return objectMapper.readValue(content, ClientTokenDTO.class);
    }

    public OrderResponseDTO createOrder(OrderDTO orderDTO) throws Exception {
        var appContext = new PayPalAppContextDTO();
        appContext.setReturnUrl("http://localhost:8080/api/checkout/success");
        orderDTO.setApplicationContext(appContext);
        var accessTokenDto = getAccessToken();
        var payload = objectMapper.writeValueAsString(orderDTO);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(createUrl(paypalConfig.getBaseUrl(), ORDER_CHECKOUT)))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenDto.getAccessToken())
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        var content = response.body();
        var orderResponse = objectMapper.readValue(content, OrderResponseDTO.class);
        saveOrder(orderResponse, orderDTO);
        return orderResponse;
    }

    public void saveOrder(OrderResponseDTO orderResponseDTO, OrderDTO orderDTO){
        var order = new Order();
        order.setPaypalOrderId(orderResponseDTO.getId());
        order.setPaypalOrderStatus(orderResponseDTO.getStatus().toString());
        order.setBalance(Integer.parseInt(orderDTO.getPurchaseUnits().get(0).getAmount().getValue()));
        order.setPassenger((Passenger) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        orderRepository.save(order);
    }

    public void confirmOrder(String orderId) throws Exception {
        var accessTokenDto = getAccessToken();
        int balance;
        var order = orderRepository.findByPaypalOrderId(orderId);
        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://api-m.sandbox.paypal.com/v2/checkout/orders/"+orderId+"/capture"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenDto.getAccessToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        balance = orderRepository.findByPaypalOrderId(orderId).getBalance() * 100;
        passengerService.addToTokenBalance(balance, order.getPassenger().getUsername());
        order.setPaypalOrderStatus(OrderStatus.APPROVED.toString());
        orderRepository.save(order);
    }


    private String encodeBasicCredentials() {
        var input = paypalConfig.getClientId() + ":" + paypalConfig.getSecret();
        return "Basic " + Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }
}
