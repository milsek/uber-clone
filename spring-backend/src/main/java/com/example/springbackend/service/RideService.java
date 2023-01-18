package com.example.springbackend.service;

import com.example.springbackend.dto.creation.*;
import com.example.springbackend.dto.display.*;
import com.example.springbackend.exception.AdequateDriverNotFoundException;
import com.example.springbackend.exception.InsufficientFundsException;
import com.example.springbackend.model.*;
import com.example.springbackend.model.helpClasses.Coordinates;
import com.example.springbackend.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.Optional;

@Service
public class RideService {

    @Autowired
    RideRepository rideRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    PassengerRepository passengerRepository;
    @Autowired
    PassengerRideRepository passengerRideRepository;
    @Autowired
    VehicleTypeRepository vehicleTypeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RouteRepository routeRepository;
    @Autowired
    AdminRepository adminRepository;
    @Autowired
    ModelMapper modelMapper;

    public Boolean orderSplitFareRide(SplitFareRideCreationDTO dto, Authentication auth) {
        Passenger passenger = (Passenger) auth.getPrincipal();
        VehicleType vehicleType = vehicleTypeRepository.findByName(dto.getVehicleType()).orElseThrow();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        int price = calculateRidePrice(dto, vehicleType);
        dto.getUsersToPay().add(passenger.getUsername());
        int fare = (int) Math.ceil(price/dto.getUsersToPay().size());
        Ride ride = createSplitFareRide(dto, price);
        if (passenger.getTokenBalance() < fare) {
            throw new InsufficientFundsException();
        }
        passenger.setTokenBalance(passenger.getTokenBalance() - fare);
        passengerRepository.save(passenger);
        createPassengerRideForUsers(dto, ride, fare, passenger);

        executorService.schedule(() -> processSplitFareRide(dto, fare, ride), 30, TimeUnit.SECONDS);
        return true;
    }

    private void createPassengerRideForUsers(SplitFareRideCreationDTO dto, Ride ride,int fare, Passenger passenger) {
        boolean agreed = false;
        for(String username : dto.getUsersToPay()){
            agreed = false;
            Passenger p = passengerRepository.findByUsername(username).get();
            if(p.getUsername() == passenger.getUsername()){
                agreed = true;
            }
            createSplitFarePassengerRide(p, ride, fare, agreed);
        }
    }

    private void createSplitFarePassengerRide(Passenger passenger, Ride ride, int fare, boolean agreed) {
        PassengerRide passengerRide = new PassengerRide();
        passengerRide.setPassenger(passenger);
        passengerRide.setRide(ride);
        passengerRide.setFare(fare);
        passengerRide.setAgreed(agreed);
        passengerRideRepository.save(passengerRide);
    }

    public void processSplitFareRide(SplitFareRideCreationDTO dto, int fare, Ride ride){
        Ride newRide = rideRepository.findById(ride.getId()).get();
        if(!newRide.getPassengersConfirmed())
            for(String username : dto.getUsersToPay()){
                PassengerRide passengerRide = passengerRideRepository.findByRideAndPassengerUsername(ride, username).get();
                Passenger passenger = passengerRide.getPassenger();
                if(passengerRide.isAgreed()){
                    passenger.setTokenBalance(passenger.getTokenBalance() + fare);
                    passengerRepository.save(passenger);
                }
            }
    }


    public Object confirmRide(RideIdDTO dto, Authentication auth) {
        Ride ride = rideRepository.findById(dto.getRideId()).get();
        Passenger passenger = (Passenger) auth.getPrincipal();
        PassengerRide currentPR = passengerRideRepository.findByRideAndPassengerUsername(ride, passenger.getUsername()).get();
        currentPR.setAgreed(true);
        passenger.setTokenBalance(passenger.getTokenBalance() - currentPR.getFare());
        passengerRideRepository.save(currentPR);
        passengerRepository.save(passenger);
        List<String> usersToPay = passengerRideRepository.getPassengersForRide(dto.getRideId());
        boolean fullyPaid = true;
        for(String username : usersToPay){
            PassengerRide passengerRide = passengerRideRepository.findByRideAndPassengerUsername(ride, username).get();
            if(!passengerRide.isAgreed()){
                fullyPaid = false;
            }
        }
        if(fullyPaid){
            ride.setPassengersConfirmed(true);
            BasicRideCreationDTO basicRideCreationDTO = modelMapper.map(ride, BasicRideCreationDTO.class);
            basicRideCreationDTO.setVehicleType(ride.getVehicleType());
            Driver driver = findDriver(basicRideCreationDTO);
            ride.setDriver(driver);
            rideRepository.save(ride);
            //TODO: send notifications
            if (driver == null) {
                throw new AdequateDriverNotFoundException();
            }
        }
        return null;
    }

    public RideSimpleDisplayDTO orderBasicRide(BasicRideCreationDTO dto, Authentication auth) {
        Passenger passenger = (Passenger) auth.getPrincipal();
        VehicleType vehicleType = vehicleTypeRepository.findByName(dto.getVehicleType()).orElseThrow();

        int price = calculateRidePrice(dto, vehicleType);
        if (passenger.getTokenBalance() < price) {
            throw new InsufficientFundsException();
        }

        Driver driver = findDriver(dto);
        if (driver == null) {
            throw new AdequateDriverNotFoundException();
        }

        // successful
        passenger.setTokenBalance(passenger.getTokenBalance() - price);
        passengerRepository.save(passenger);
        Ride ride = createRide(dto, price, driver);
        createPassengerRide(passenger, ride);

        //TODO: send notifications
        RideSimpleDisplayDTO rideDisplayDTO = createBasicRideSimpleDisplayDTO(ride, driver);

        return rideDisplayDTO;
    }

    private Driver findDriver(BasicRideCreationDTO dto) {
        CoordinatesCreationDTO startCoordinates = dto.getActualRoute().getWaypoints().get(0);

        List<Driver> potentialClosestDriver = driverRepository.getClosestFreeDriver(startCoordinates.getLat(),
                startCoordinates.getLng(), dto.isBabySeat(), dto.isPetFriendly(), dto.getVehicleType(),
                PageRequest.of(0, 1)).stream().toList();
        if (!potentialClosestDriver.isEmpty()) return potentialClosestDriver.get(0);

        List<Driver> closeBusyDriversWithNoNextRide = driverRepository
                .getCloseBusyDriversWithNoNextRide(startCoordinates.getLat(), startCoordinates.getLng(),
                        dto.isBabySeat(), dto.isPetFriendly(), dto.getVehicleType());

        if (closeBusyDriversWithNoNextRide.isEmpty()) return null;
        else if (closeBusyDriversWithNoNextRide.size() == 1) return closeBusyDriversWithNoNextRide.get(0);

        Driver bestChoice = closeBusyDriversWithNoNextRide.get(0);
        long minSecondsUntilRideEnd = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                bestChoice.getVehicle().getCoordinatesChangedAt()) - bestChoice.getVehicle().getExpectedTripTime();

        for (Driver d : closeBusyDriversWithNoNextRide) {
            long secondsUntilRideEnd = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                    d.getVehicle().getCoordinatesChangedAt()) - d.getVehicle().getExpectedTripTime();
            if (secondsUntilRideEnd < minSecondsUntilRideEnd) {
                bestChoice = d;
                minSecondsUntilRideEnd = secondsUntilRideEnd;
            }
        }

        return bestChoice;
    }



    private void createPassengerRide(Passenger passenger, Ride ride) {
        PassengerRide passengerRide = new PassengerRide();
        passengerRide.setPassenger(passenger);
        passengerRide.setRide(ride);
        passengerRide.setFare(ride.getPrice());
        passengerRide.setAgreed(true);
        passengerRideRepository.save(passengerRide);
    }

    private Ride createRide(BasicRideCreationDTO dto, int price, Driver driver) {
        Ride ride = new Ride();
        Route actualRoute = createRouteFromDto(dto.getActualRoute());
        routeRepository.save(actualRoute);
        Route expectedRoute = null;
        if (dto.getExpectedRoute() != null) {
            expectedRoute = createRouteFromDto(dto.getExpectedRoute());
            routeRepository.save(expectedRoute);
        }
        ride.setDistance(dto.getDistance());
        ride.setActualRoute(actualRoute);
        ride.setExpectedRoute(expectedRoute);
        ride.setDriverCancelled("");
        ride.setRejected(false);
        ride.setStartTime(null);
        ride.setEndTime(null);
        ride.setExpectedTime(dto.getExpectedTime());
        ride.setCreatedAt(LocalDateTime.now());
        ride.setDriverInconsistency(false);
        ride.setPrice(price);
        ride.setPassengersConfirmed(true);
        ride.setDriver(driver);
        rideRepository.save(ride);

        if (driver.getCurrentRide() == null) {
            driver.setCurrentRide(ride);
        } else {
            driver.setNextRide(ride);
        }
        driverRepository.save(driver);

        return ride;
    }

    private Ride createSplitFareRide(BasicRideCreationDTO dto, int price) {
        Ride ride = new Ride();
        Route actualRoute = createRouteFromDto(dto.getActualRoute());
        routeRepository.save(actualRoute);
        Route expectedRoute = null;
        if (dto.getExpectedRoute() != null) {
            expectedRoute = createRouteFromDto(dto.getExpectedRoute());
            routeRepository.save(expectedRoute);
        }
        ride.setDistance(dto.getDistance());
        ride.setActualRoute(actualRoute);
        ride.setExpectedRoute(expectedRoute);
        ride.setDriverCancelled("");
        ride.setRejected(false);
        ride.setStartTime(null);
        ride.setEndTime(null);
        ride.setExpectedTime(dto.getExpectedTime());
        ride.setVehicleType(dto.getVehicleType());
        ride.setCreatedAt(LocalDateTime.now());
        ride.setDriverInconsistency(false);
        ride.setPrice(price);
        ride.setPassengersConfirmed(false);
        rideRepository.save(ride);
        return ride;
    }

    private int calculateRidePrice(BasicRideCreationDTO dto, VehicleType vehicleType) {
        return (int) Math.round(vehicleType.getPrice() + dto.getDistance() * 120);
    }

    public RideSimpleDisplayDTO createBasicRideSimpleDisplayDTO(Ride ride, Driver driver) {
        RideSimpleDisplayDTO rideDisplayDTO = modelMapper.map(ride, RideSimpleDisplayDTO.class);
        rideDisplayDTO.setDriver(modelMapper.map(driver, DriverSimpleDisplayDTO.class));
        if (ride.getExpectedRoute() != null)
            rideDisplayDTO.setRoute(createRouteDisplayDtoFromRoute(ride.getExpectedRoute()));
        else
            rideDisplayDTO.setRoute(createRouteDisplayDtoFromRoute(ride.getActualRoute()));
        return rideDisplayDTO;
    }

    private Route createRouteFromDto(RouteCreationDTO dto) {
        Route route = new Route();
        route.setWaypoints(dto.getWaypoints().stream().map(latLng ->
                modelMapper.map(latLng, Coordinates.class)).toList());
        route.setCoordinates(dto.getCoordinates().stream().map(latLng ->
                modelMapper.map(latLng, Coordinates.class)).toList());
        return route;
    }

    private RouteDisplayDTO createRouteDisplayDtoFromRoute(Route route) {
        RouteDisplayDTO dto = new RouteDisplayDTO();
        dto.setWaypoints(route.getWaypoints().stream().map(latLng ->
                modelMapper.map(latLng, CoordinatesDisplayDTO.class)).toList());
        dto.setCoordinates(route.getCoordinates().stream().map(latLng ->
                modelMapper.map(latLng, CoordinatesDisplayDTO.class)).toList());
        return dto;
    }

    public Page<RideHistoryDisplayDTO> getRideHistory(String username, Authentication authentication, Pageable paging) {
        User user = (User) authentication.getPrincipal();
        if(username.isEmpty()){
            username = user.getUsername();
        }
        else{
            if(!adminRepository.findById(username).isPresent()){
                return null;
            }
        }
        Page<PassengerRide> passengerRides = passengerRideRepository.findByPassengerUsername(username, paging);
        return passengerRides.map(passengerRide -> modelMapper.map(passengerRide.getRide(), RideHistoryDisplayDTO.class));
    }

    public DetailedRideHistoryPassengerDTO detailedRideHistoryPassenger(Integer rideId, Authentication authentication) {
        Optional<Ride> optRide = rideRepository.findById(rideId);
        List<PassengerRide> passengerRides = passengerRideRepository.findByRideId(rideId);
        if(optRide.isPresent()){
            DetailedRideHistoryPassengerDTO returnDTO = modelMapper.map(optRide.get(), DetailedRideHistoryPassengerDTO.class);
            for(PassengerRide passengerRide : passengerRides){
                if(passengerRide.getDriverRating() != 0)
                returnDTO.getDriverRating().put(passengerRide.getPassenger().getUsername(),passengerRide.getDriverRating());
                if(passengerRide.getVehicleRating() != 0)
                    returnDTO.getVehicleRating().put(passengerRide.getPassenger().getUsername(),passengerRide.getVehicleRating());
            }
            return returnDTO;
        }
        return null;
    }

    public DetailedRideHistoryDriverDTO detailedRideHistoryDriver(Integer rideId, Authentication authentication) {
        List<PassengerRide> passengerRides = passengerRideRepository.findByRideId(rideId);
        DetailedRideHistoryDriverDTO detailedRideHistoryDriverDTO = new  DetailedRideHistoryDriverDTO();
        passengerRides.stream().forEach(passengerRide ->
                detailedRideHistoryDriverDTO.addPassenger(modelMapper.map(passengerRide.getPassenger(), PassengerDisplayDTO.class)));
        return detailedRideHistoryDriverDTO;
    }

    public Boolean leaveReview(ReviewDTO reviewDTO, Authentication authentication) {
        Passenger passenger = (Passenger) authentication.getPrincipal();
        Optional<PassengerRide> optPassengerRide = passengerRideRepository.findByRideIdAndPassengerUsername(reviewDTO.getRideId(),passenger.getUsername());
        if(optPassengerRide.isPresent()){
            PassengerRide passengerRide = optPassengerRide.get();
            passengerRide.setComment(reviewDTO.getComment());
            passengerRide.setVehicleRating(reviewDTO.getVehicleRating());
            passengerRide.setDriverRating(reviewDTO.getDriverRating());
            passengerRideRepository.save(passengerRide);
            return true;
        }
        return false;
    }
}
