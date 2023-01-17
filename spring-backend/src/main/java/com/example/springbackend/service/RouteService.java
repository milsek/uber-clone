package com.example.springbackend.service;

import com.example.springbackend.dto.creation.RideIdDTO;
import com.example.springbackend.dto.creation.RouteIdDTO;
import com.example.springbackend.model.Passenger;
import com.example.springbackend.model.Route;
import com.example.springbackend.repository.PassengerRepository;
import com.example.springbackend.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class RouteService {

    @Autowired
    RouteRepository routeRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Boolean markRouteAsFavourite(RouteIdDTO routeIdDTO, Authentication authentication) {
        Optional<Route> route = routeRepository.findById(routeIdDTO.getRouteId());
        if(route.isPresent()){
            Passenger passenger = (Passenger) authentication.getPrincipal();
            passenger.getFavouriteRoutes().add(route.get());
            passengerRepository.save(passenger);
            return true;
        }
        return false;
    }
    public Boolean unmarkRouteAsFavourite(RouteIdDTO routeIdDTO, Authentication authentication) {
        Optional<Route> route = routeRepository.findById(routeIdDTO.getRouteId());
        if(route.isPresent()){
            Passenger passenger = (Passenger) authentication.getPrincipal();
            for(Route favouriteRoute : passenger.getFavouriteRoutes()){
                if(Objects.equals(favouriteRoute.getId(), route.get().getId())){
                    passenger.getFavouriteRoutes().remove(favouriteRoute);
                    passengerRepository.save(passenger);
                    return true;
                }
            }
        }
        return false;
    }

    public Page<Route> getFavouriteRoutes(Pageable paging, Authentication authentication){
        Passenger passenger = (Passenger) authentication.getPrincipal();
        Page<Route> routes = routeRepository.getRoutesByPassengersContains(passenger, paging);
        return routes;
    }
}
