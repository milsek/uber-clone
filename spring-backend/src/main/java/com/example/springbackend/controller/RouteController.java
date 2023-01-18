package com.example.springbackend.controller;

import com.example.springbackend.dto.creation.BasicRideCreationDTO;
import com.example.springbackend.dto.creation.RouteIdDTO;
import com.example.springbackend.dto.creation.SplitFareRideCreationDTO;
import com.example.springbackend.dto.display.RideSimpleDisplayDTO;
import com.example.springbackend.model.Route;
import com.example.springbackend.service.RideService;
import com.example.springbackend.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/routes", produces = MediaType.APPLICATION_JSON_VALUE)
public class RouteController {
    @Autowired
    RouteService routeService;


    @PostMapping("/mark-route-as-favourite")
    public ResponseEntity<Boolean> markRouteAsFavourite(@Valid @RequestBody RouteIdDTO routeIdDTO, Authentication authentication){
        return ResponseEntity.ok(routeService.markRouteAsFavourite(routeIdDTO, authentication));
    }
    @PostMapping("/unmark-route-as-favourite")
    public ResponseEntity<Boolean> unmarkRouteAsFavourite(@Valid @RequestBody RouteIdDTO routeIdDTO, Authentication authentication){
        return ResponseEntity.ok(routeService.unmarkRouteAsFavourite(routeIdDTO, authentication));
    }
    @GetMapping("/favourite-routes")
    public Page<Route> getFavouriteRoutes(@RequestParam Integer page, @RequestParam Integer amount, Authentication authentication){
        Pageable paging = PageRequest.of(page, amount);
        return routeService.getFavouriteRoutes(paging, authentication);
    }
}
