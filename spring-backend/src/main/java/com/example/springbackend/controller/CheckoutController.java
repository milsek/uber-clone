package com.example.springbackend.controller;

import com.example.springbackend.config.PayPalService;
import com.example.springbackend.model.Passenger;
import com.example.springbackend.repository.OrderRepository;
import com.example.springbackend.dto.paypal.OrderStatus;
import com.example.springbackend.repository.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.springbackend.dto.paypal.OrderDTO;
import com.example.springbackend.dto.paypal.OrderResponseDTO;
import com.example.springbackend.dto.paypal.PayPalAppContextDTO;
import com.example.springbackend.model.Order;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "api/checkout")
public class CheckoutController {

    @Autowired
    private PayPalService payPalService;
    @Autowired
    private OrderRepository orderDAO;

    @PostMapping
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<OrderResponseDTO> checkout(@RequestBody OrderDTO orderDTO) throws Exception {
        var orderResponse = payPalService.createOrder(orderDTO);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping(value = "/success")
    public ResponseEntity<String> paymentSuccess(HttpServletRequest request) throws Exception {
        payPalService.confirmOrder(request.getParameter("token"));
        return ResponseEntity.ok().body("Payment success");
    }

}
