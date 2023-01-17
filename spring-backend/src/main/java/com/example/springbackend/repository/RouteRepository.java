package com.example.springbackend.repository;

import com.example.springbackend.model.Admin;
import com.example.springbackend.model.Passenger;
import com.example.springbackend.model.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface RouteRepository extends JpaRepository<Route, Integer> {


    Page<Route> getRoutesByPassengersContains(Passenger passenger, Pageable paging);
}
