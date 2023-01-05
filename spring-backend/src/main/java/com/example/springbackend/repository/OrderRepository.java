package com.example.springbackend.repository;

import com.example.springbackend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Order findByPaypalOrderId(String paypalOrderId);
}
