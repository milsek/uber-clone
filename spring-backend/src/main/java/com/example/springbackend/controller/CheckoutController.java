package com.example.springbackend.controller;

import com.example.springbackend.service.PayPalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.springbackend.dto.paypal.OrderDTO;
import com.example.springbackend.dto.paypal.OrderResponseDTO;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@RestController
@RequestMapping(value = "api/checkout")
public class CheckoutController {

    @Autowired
    private PayPalService payPalService;

    @PostMapping
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<OrderResponseDTO> checkout(@RequestBody OrderDTO orderDTO) throws Exception {
        var orderResponse = payPalService.createOrder(orderDTO);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping(value = "/success")
    public ResponseEntity<String> paymentSuccess(HttpServletRequest request) throws Exception {
        payPalService.confirmOrder(request.getParameter("token"));
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:4200/account"))
                .build();
    }

}
