package com.example.springbackend.service;

import com.example.springbackend.dto.creation.*;
import com.example.springbackend.dto.display.*;
import com.example.springbackend.exception.*;
import com.example.springbackend.model.*;
import com.example.springbackend.model.helpClasses.Coordinates;
import com.example.springbackend.model.helpClasses.ReportParameter;
import com.example.springbackend.repository.*;
import com.example.springbackend.websocket.MessageType;
import com.example.springbackend.websocket.WSMessage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    VehicleRepository vehicleRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    UserService userService;
    @Autowired
    SimulatorService simulatorService;
    private final SimpMessagingTemplate template;
    @Autowired
    RideService(SimpMessagingTemplate template) {
        this.template = template;
    }

    public RideSimpleDisplayDTO orderBasicRide(BasicRideCreationDTO dto, Authentication auth) {
        Passenger passenger = (Passenger) auth.getPrincipal();
        VehicleType vehicleType = vehicleTypeRepository.findByName(dto.getVehicleType()).orElseThrow();

        Optional<PassengerRide> currentPassengerRide = passengerRideRepository.getCurrentPassengerRide(passenger);
        if (currentPassengerRide.isPresent()) {
            throw new PassengerAlreadyHasAnActiveRideException();
        }

        int price = calculateRidePrice(dto, vehicleType);
        if (passenger.getTokenBalance() < price) {
            throw new InsufficientFundsException();
        }

        Driver driver = null;
        Ride ride = createBasicRide(dto, price, null);
        PassengerRide passengerRide = createPassengerRide(passenger, ride);

        if (dto.getDelayInMinutes() == 0) {
            driver = findDriver(ride);
            if (driver == null) {
                ride.setStatus(RideStatus.CANCELLED);
                rideRepository.save(ride);
                throw new AdequateDriverNotFoundException();
            } else {
                linkDriverAndRide(driver, ride);
                passenger.setTokenBalance(passenger.getTokenBalance() - price);
                passengerRepository.save(passenger);
                sendRefreshMessage(driver.getUsername());
            }
        } else {
            if (dto.getDelayInMinutes() < 20) {
                ride.setStatus(RideStatus.CANCELLED);
                rideRepository.save(ride);
                throw new ReservationTooSoonException();
            }
            passenger.setTokenBalance(passenger.getTokenBalance() - price);
            passengerRepository.save(passenger);
            List<PassengerRide> passengerRides = new ArrayList<>();
            passengerRides.add(passengerRide);
            handleNotificationsAndProcessReservations(ride, passengerRides);
        }

        RideSimpleDisplayDTO rideDisplayDTO = createBasicRideSimpleDisplayDTO(passengerRide, driver);
        return rideDisplayDTO;
    }

    private void handleNotificationsAndProcessReservations(Ride ride, List<PassengerRide> passengerRides) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        List<String> passengerUsernames = passengerRides.stream().map(pr ->
                pr.getPassenger().getUsername()).toList();
        executorService.schedule(() -> notifyPassengersAboutReservation(passengerUsernames, 15),
                ride.getDelayInMinutes() - 15, TimeUnit.SECONDS); // should be minutes in production
        executorService.schedule(() -> notifyPassengersAboutReservation(passengerUsernames, 10),
                ride.getDelayInMinutes() - 10, TimeUnit.SECONDS); // should be minutes in production
        executorService.schedule(() -> notifyPassengersAboutReservation(passengerUsernames, 5),
                ride.getDelayInMinutes() - 5, TimeUnit.SECONDS); // should be minutes in production
        executorService.schedule(() -> processReservation(ride, passengerRides),
                ride.getDelayInMinutes(), TimeUnit.SECONDS); // should be minutes in production
    }

    public Boolean orderSplitFareRide(SplitFareRideCreationDTO dto, Authentication auth) {
        Passenger passenger = (Passenger) auth.getPrincipal();
        VehicleType vehicleType = vehicleTypeRepository.findByName(dto.getVehicleType()).orElseThrow();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        int price = calculateRidePrice(dto, vehicleType);
        dto.getUsersToPay().add(passenger.getUsername());
        int fare = (int) Math.ceil(price/dto.getUsersToPay().size());

        checkIfSplitFareRideIsValid(dto, passenger, fare);

        Ride ride = createSplitFareRide(dto, price);
        passenger.setTokenBalance(passenger.getTokenBalance() - fare);
        passengerRepository.save(passenger);
        createPassengerRideForUsers(dto, ride, fare, passenger);

        dto.getUsersToPay().stream().forEach(username -> {
            if (!username.equals(passenger.getUsername()))
                sendRefreshMessage(username);
        });

        executorService.schedule(() -> processSplitFareRide(dto, ride), 30, TimeUnit.SECONDS);
        return true;
    }

    private void processReservation(Ride ride, List<PassengerRide> passengerRides) {
        Driver driver = findDriver(ride);
        if (driver == null) {
            sendMessageToMultiplePassengers(
                    passengerRides.stream().map(pr -> pr.getPassenger().getUsername()).toList(),
                    "Adequate driver was not found.",
                    MessageType.RIDE_ERROR);
            ride.setStatus(RideStatus.CANCELLED);
            rideRepository.save(ride);
            refundPassengers(passengerRides);
            throw new AdequateDriverNotFoundException();
        } else {
            linkDriverAndRide(driver, ride);
            sendRefreshMessage(driver.getUsername());
            sendRefreshMessageToMultipleUsers(
                    passengerRides.stream().map(pr -> pr.getPassenger().getUsername()).toList());
        }
    }

    private void linkDriverAndRide(Driver driver, Ride ride) {
        ride.setDriver(driver);
        ride.setStatus(RideStatus.DRIVER_ARRIVING);
        rideRepository.save(ride);
        if (driver.getCurrentRide() == null) {
            driver.setCurrentRide(ride);
            directDriverToCurrentRideStart(driver, ride);
        } else {
            driver.setNextRide(ride);
        }
        driverRepository.save(driver);
    }

    private void directDriverToCurrentRideStart(Driver driver, Ride ride) {
        directDriverToLocation(driver, ride.getRoute().getWaypoints().get(0));
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> markDriverArrived(ride.getId()),
                driver.getVehicle().getExpectedTripTime(), TimeUnit.SECONDS);
    }

    private void markDriverArrived(Integer rideId) {
        Ride ride = rideRepository.findById(rideId).get();
        if (ride.getStatus() == RideStatus.CANCELLED) return;
        ride.setStatus(RideStatus.DRIVER_ARRIVED);
        rideRepository.save(ride);
        sendRefreshMessageToDriverAndAllPassengers(ride);
    }

    private void markArrivedAtDestination(Integer rideId, int waypointIndex) {
        Ride ride = rideRepository.findById(rideId).get();
        if (ride.getStatus() == RideStatus.CANCELLED) return;
        if (ride.getRoute().getWaypoints().size() > waypointIndex + 1) {
            directDriverToLocation(ride.getDriver(), ride.getRoute().getWaypoints().get(waypointIndex + 1));
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.schedule(() -> markArrivedAtDestination(ride.getId(), waypointIndex + 1),
                    ride.getDriver().getVehicle().getExpectedTripTime(), TimeUnit.SECONDS);
        } else {
            ride.setStatus(RideStatus.ARRIVED_AT_DESTINATION);
            rideRepository.save(ride);
        }
        sendRefreshMessageToDriverAndAllPassengers(ride);
    }

    private void directDriverToLocation(Driver driver, Coordinates coordinates) {
        Vehicle vehicle = driver.getVehicle();
        vehicle.setNextCoordinates(coordinates);
        vehicle.setRideActive(true);
        vehicle.setCoordinatesChangedAt(LocalDateTime.now());
        long estimatedTime = simulatorService.getEstimatedTime(vehicle);
        vehicle.setExpectedTripTime(estimatedTime);
        vehicleRepository.save(vehicle);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> simulatorService.arriveAtLocation(vehicle, true),
                estimatedTime, TimeUnit.SECONDS);
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

    public void processSplitFareRide(SplitFareRideCreationDTO dto, Ride ride) {
        Ride newRide = rideRepository.findById(ride.getId()).get();
        if (newRide.getStatus() != RideStatus.CANCELLED && !newRide.getPassengersConfirmed()) {
            for (String username : dto.getUsersToPay()) {
                sendMessageToPassenger(username,
                        "The ride is cancelled because one of the passengers did not respond to the invitation.",
                        MessageType.RIDE_ERROR);
            }
            List<PassengerRide> passengerRides = passengerRideRepository.findByRide(ride);
            refundPassengers(passengerRides);
            ride.setStatus(RideStatus.CANCELLED);
            rideRepository.save(ride);
        }
    }

    public Object confirmRide(RideIdDTO dto, Authentication auth) {
        Ride ride = rideRepository.findById(dto.getRideId()).get();
        Passenger passenger = (Passenger) auth.getPrincipal();
        List<PassengerRide> passengerRides = passengerRideRepository.findByRide(ride);
        List<String> usersToPay = passengerRides.stream().map(pr -> pr.getPassenger().getUsername()).toList();

        PassengerRide currentPR = passengerRideRepository.findByRideAndPassengerUsername(ride, passenger.getUsername()).get();
        if (passenger.getTokenBalance() < currentPR.getFare()) {
            for (String username : usersToPay) {
                sendMessageToPassenger(username,
                        "Ride is cancelled due to insufficient funds.",
                        MessageType.RIDE_ERROR);
            }
            ride.setStatus(RideStatus.CANCELLED);
            rideRepository.save(ride);
            refundPassengers(passengerRides);
            throw new InsufficientFundsException();
        }
        currentPR.setAgreed(true);
        passenger.setTokenBalance(passenger.getTokenBalance() - currentPR.getFare());
        passengerRideRepository.save(currentPR);
        passengerRepository.save(passenger);

        boolean fullyPaid = true;
        for (String username : usersToPay) {
            PassengerRide passengerRide = passengerRideRepository.findByRideAndPassengerUsername(ride, username).get();
            if (!passengerRide.isAgreed()) {
                fullyPaid = false;
            }
        }
        if (fullyPaid) {
            ride.setPassengersConfirmed(true);
            ride.setStatus(RideStatus.RESERVED);
            rideRepository.save(ride);
            if (ride.getDelayInMinutes() == 0) {
                processReservation(ride, passengerRides);
            } else {
                sendRefreshMessageToMultipleUsers(usersToPay);
                handleNotificationsAndProcessReservations(ride, passengerRides);
            }
        }
        return null;
    }

    public Boolean rejectRide(RideIdDTO dto, Authentication auth) {
        Ride ride = rideRepository.findById(dto.getRideId()).orElseThrow();
        Passenger passenger = (Passenger) auth.getPrincipal();
        List<PassengerRide> passengerRides = passengerRideRepository.findByRide(ride);
        PassengerRide currentPassengerRide = passengerRideRepository
                .findByRideAndPassengerUsername(ride, passenger.getUsername()).orElseThrow();
        if (!ride.getPassengersConfirmed() && !currentPassengerRide.isAgreed()) {
            for (PassengerRide passengerRide : passengerRides) {
                Passenger ridePassenger = passengerRide.getPassenger();
                if (!ridePassenger.getUsername().equals(passenger.getUsername()))
                    sendMessageToPassenger(ridePassenger.getUsername(),
                            "A passenger has rejected the ride.",
                            MessageType.RIDE_ERROR);
            }
            refundPassengers(passengerRides);
            ride.setStatus(RideStatus.CANCELLED);
            rideRepository.save(ride);
            return true;
        }
        return false;
    }

    public Boolean reportInconsistency(RideIdDTO dto, Authentication auth) {
        Passenger passenger = (Passenger) auth.getPrincipal();
        Ride ride = rideRepository.findById(dto.getRideId()).orElseThrow();
        Optional<PassengerRide> passengerRide =
                passengerRideRepository.findByRideAndPassengerUsername(ride, passenger.getUsername());
        if (passengerRide.isPresent()) {
            ride.setDriverInconsistencyReported(true);
            rideRepository.save(ride);
            sendRefreshMessageToDriverAndAllPassengers(ride);
            return true;
        } else {
            throw new RideDoesNotBelongToPassengerException();
        }
    }

    private void refundPassengers(List<PassengerRide> passengerRides) {
        for (PassengerRide passengerRide : passengerRides) {
            Passenger ridePassenger = passengerRide.getPassenger();
            if (passengerRide.isAgreed()) {
                ridePassenger.setTokenBalance(ridePassenger.getTokenBalance() + passengerRide.getFare());
                passengerRepository.save(ridePassenger);
            }
        }
    }

    private void checkIfSplitFareRideIsValid(SplitFareRideCreationDTO dto, Passenger passenger, int fare) {
        dto.getUsersToPay().stream().forEach(email -> {
            if (!userService.userExists(email)) {
                throw new UserDoesNotExistException("A co-passenger's email does not exist in the system.");
            }
        });
        if (dto.getUsersToPay().stream().distinct().count() != dto.getUsersToPay().size()) {
            throw new LinkedPassengersNotAllDistinctException();
        }
        List<PassengerRide> currentPassengerRides =
                passengerRideRepository.getCurrentPassengerRidesByUsername(dto.getUsersToPay());
        if (!currentPassengerRides.isEmpty()) {
            throw new PassengerAlreadyHasAnActiveRideException();
        }

        if (passenger.getTokenBalance() < fare) {
            throw new InsufficientFundsException();
        }

        if (dto.getDelayInMinutes() != 0 && dto.getDelayInMinutes() < 20) {
            throw new ReservationTooSoonException();
        }
    }

    public Boolean beginRide(RideIdDTO dto, Authentication auth) {
        Driver driver = (Driver) auth.getPrincipal();
        Ride ride = rideRepository.findById(dto.getRideId()).orElseThrow();
        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartTime(LocalDateTime.now());
        rideRepository.save(ride);
        directDriverToLocation(driver, ride.getRoute().getWaypoints().get(1));
        sendRefreshMessageToDriverAndAllPassengers(ride);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> markArrivedAtDestination(ride.getId(), 1),
                ride.getDriver().getVehicle().getExpectedTripTime(), TimeUnit.SECONDS);
        return true;
    }

    public Boolean completeRide(RideIdDTO dto, Authentication auth) {
        Driver driver = (Driver) auth.getPrincipal();
        Ride ride = rideRepository.findById(dto.getRideId()).orElseThrow();
        ride.setStatus(RideStatus.COMPLETED);
        ride.setEndTime(LocalDateTime.now());
        rideRepository.save(ride);

        driver.setRidesCompleted(driver.getRidesCompleted() + 1);
        driver.setDistanceTravelled(driver.getDistanceTravelled() + ride.getDistance());
        driverRepository.save(driver);

        List<PassengerRide> passengerRides = passengerRideRepository.findByRide(ride);
        sendMessageToMultiplePassengers(passengerRides.stream().map(pr -> pr.getPassenger().getUsername()).toList(),
                "", MessageType.RIDE_COMPLETE);

        updateCurrentDriverRide(driver);
        sendRefreshMessage(driver.getUsername());
        return true;
    }

    private void updateCurrentDriverRide(Driver driver) {
        driver.setCurrentRide(driver.getNextRide());
        driver.setNextRide(null);
        driverRepository.save(driver);
        if (driver.getCurrentRide() != null)
            directDriverToCurrentRideStart(driver, driver.getCurrentRide());
        else {
            driver.getVehicle().setRideActive(false);
            vehicleRepository.save(driver.getVehicle());
        }
    }

    public Boolean driverRejectRide(DriverRideRejectionCreationDTO dto, Authentication auth) {
        Ride ride = rideRepository.findById(dto.getRideId()).orElseThrow();
        ride.setDriverRejectionReason(dto.getReason());
        rideRepository.save(ride);
        return true;
    }

    public List<DriverRideRejectionDisplayDTO> getDriverRideRejectionRequests(Authentication auth) {
        List<Ride> ridesPendingRejection = rideRepository.getRidesPendingRejection();
        return ridesPendingRejection.stream().map(r -> modelMapper.map(r, DriverRideRejectionDisplayDTO.class)).toList();
    }

    public Boolean acceptDriverRideRejection(DriverRideRejectionVerdictCreationDTO dto, Authentication auth) {
        Ride ride = rideRepository.findById(dto.getRideId()).orElseThrow();

        if (!dto.isAccepted()) {
            ride.setDriverRejectionReason(null);
            rideRepository.save(ride);
            sendMessageToDriver(ride.getDriver().getUsername(),
                    "Your rejection reason was deemed invalid.",
                    MessageType.RIDE_ERROR);
            return true;
        }

        ride.setStatus(RideStatus.CANCELLED);
        rideRepository.save(ride);

        Driver driver = ride.getDriver();
        Optional<Ride> optionalNextRide = rideRepository.findById(driver.getNextRide().getId());
        driver.setActive(false);
        driver.setCurrentRide(null);
        driver.setNextRide(null);
        driverRepository.save(driver);

        Vehicle vehicle = driver.getVehicle();
        vehicle.setCurrentCoordinates(vehicle.getNextCoordinates());
        vehicle.setExpectedTripTime(0);
        vehicle.setCoordinatesChangedAt(LocalDateTime.now());
        vehicleRepository.save(vehicle);

        List<PassengerRide> passengerRides = passengerRideRepository.findByRide(ride);
        handleRejectedRidePassengers(passengerRides);

        if (optionalNextRide.isPresent()) {
            Ride nextRide = optionalNextRide.get();
            nextRide.setStatus(RideStatus.CANCELLED);
            rideRepository.save(nextRide);
            List<PassengerRide> nextRidePassengerRides = passengerRideRepository.findByRide(nextRide);
            handleRejectedRidePassengers(nextRidePassengerRides);
        }

        sendMessageToDriver(ride.getDriver().getUsername(),
                "Your rejection is accepted and your rides are cancelled.",
                MessageType.RIDE_UPDATE);

        return true;
    }

    private void handleRejectedRidePassengers(List<PassengerRide> passengerRides) {
        for (PassengerRide passengerRide : passengerRides) {
            Passenger ridePassenger = passengerRide.getPassenger();
            sendMessageToPassenger(ridePassenger.getUsername(),
                    "The driver rejected the ride.",
                    MessageType.RIDE_ERROR);
            if (passengerRide.isAgreed()) {
                ridePassenger.setTokenBalance(ridePassenger.getTokenBalance() + passengerRide.getFare());
                passengerRepository.save(ridePassenger);
            }
        }
    }

    private Driver findDriver(Ride ride) {
        Coordinates startCoordinates = ride.getRoute().getWaypoints().get(0);
        List<Driver> potentialClosestDriver = driverRepository.getClosestFreeDriver(startCoordinates.getLat(),
                startCoordinates.getLng(), ride.isBabySeatRequested(), ride.isPetFriendlyRequested(), ride.getVehicleType(),
                PageRequest.of(0, 1)).stream().toList();
        if (!potentialClosestDriver.isEmpty()) return potentialClosestDriver.get(0);

        List<Driver> closeBusyDriversWithNoNextRide = driverRepository
                .getCloseBusyDriversWithNoNextRide(startCoordinates.getLat(), startCoordinates.getLng(),
                        ride.isBabySeatRequested(), ride.isPetFriendlyRequested(), ride.getVehicleType());

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

    private PassengerRide createPassengerRide(Passenger passenger, Ride ride) {
        PassengerRide passengerRide = new PassengerRide();
        passengerRide.setPassenger(passenger);
        passengerRide.setRide(ride);
        passengerRide.setFare(ride.getPrice());
        passengerRide.setAgreed(true);
        passengerRideRepository.save(passengerRide);
        return passengerRide;
    }

    private Ride createBasicRide(BasicRideCreationDTO dto, int price, Driver driver) {
        Ride ride = new Ride();
        Route route = createRouteFromDto(dto.getRoute());
        routeRepository.save(route);
        ride.setDistance(dto.getDistance());
        ride.setRoute(route);
        ride.setDriverRejectionReason(null);
        ride.setStatus(RideStatus.RESERVED);
        ride.setPetFriendlyRequested(dto.isPetFriendly());
        ride.setBabySeatRequested(dto.isBabySeat());
        ride.setDelayInMinutes(dto.getDelayInMinutes());
        ride.setStartAddress(dto.getStartAddress());
        ride.setDestinationAddress(dto.getDestinationAddress());
        ride.setStartTime(null);
        ride.setEndTime(null);
        ride.setExpectedTime(dto.getExpectedTime());
        ride.setVehicleType(dto.getVehicleType());
        ride.setCreatedAt(LocalDateTime.now());
        ride.setDriverInconsistencyReported(false);
        ride.setPrice(price);
        ride.setPassengersConfirmed(true);
        ride.setDriver(driver);
        rideRepository.save(ride);
        return ride;
    }

    private Ride createSplitFareRide(BasicRideCreationDTO dto, int price) {
        Ride ride = new Ride();
        Route route = createRouteFromDto(dto.getRoute());
        routeRepository.save(route);
        ride.setDistance(dto.getDistance());
        ride.setRoute(route);
        ride.setDriverRejectionReason(null);
        ride.setStatus(RideStatus.PENDING_CONFIRMATION);
        ride.setPetFriendlyRequested(dto.isPetFriendly());
        ride.setBabySeatRequested(dto.isBabySeat());
        ride.setDelayInMinutes(dto.getDelayInMinutes());
        ride.setStartAddress(dto.getStartAddress());
        ride.setDestinationAddress(dto.getDestinationAddress());
        ride.setStartTime(null);
        ride.setEndTime(null);
        ride.setExpectedTime(dto.getExpectedTime());
        ride.setVehicleType(dto.getVehicleType());
        ride.setCreatedAt(LocalDateTime.now());
        ride.setDriverInconsistencyReported(false);
        ride.setPrice(price);
        ride.setPassengersConfirmed(false);
        rideRepository.save(ride);
        return ride;
    }

    private int calculateRidePrice(BasicRideCreationDTO dto, VehicleType vehicleType) {
        return (int) Math.round(vehicleType.getPrice() + dto.getDistance() * 120);
    }

    public RideSimpleDisplayDTO createBasicRideSimpleDisplayDTO(PassengerRide pr, Driver driver) {
        Ride ride = pr.getRide();
        RideSimpleDisplayDTO rideDisplayDTO = modelMapper.map(ride, RideSimpleDisplayDTO.class);
        rideDisplayDTO.setDriver(driver != null ? modelMapper.map(driver, DriverSimpleDisplayDTO.class) : null);
        rideDisplayDTO.setAllConfirmed(ride.getPassengersConfirmed());
        rideDisplayDTO.setPassengerConfirmed(pr.isAgreed());
        rideDisplayDTO.setRoute(createRouteDisplayDtoFromRoute(ride.getRoute()));
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

    public RouteDisplayDTO createRouteDisplayDtoFromRoute(Route route) {
        RouteDisplayDTO dto = new RouteDisplayDTO();
        dto.setWaypoints(route.getWaypoints().stream().map(latLng ->
                modelMapper.map(latLng, CoordinatesDisplayDTO.class)).toList());
        dto.setCoordinates(route.getCoordinates().stream().map(latLng ->
                modelMapper.map(latLng, CoordinatesDisplayDTO.class)).toList());
        return dto;
    }

    public Page<RideHistoryDisplayDTO> getRideHistory(Authentication auth, Pageable paging, String username) {
        User user = (User) auth.getPrincipal();
        if (username.isEmpty()) {
            username = user.getUsername();
        }
        else {
            if (!user.getRoles().get(0).getName().equals("ROLE_ADMIN")) {
                return null;
            }
        }
        Page<PassengerRide> passengerRides = passengerRideRepository.findByPassengerUsername(username, paging);
        Page<RideHistoryDisplayDTO> page = passengerRides.map(passengerRide -> modelMapper
                .map(passengerRide.getRide(), RideHistoryDisplayDTO.class));
        page.getContent().stream().map(entry -> {
            entry.setDriverRating(passengerRides.stream()
                    .filter(pr -> pr.getRide().getId() == entry.getId())
                    .findFirst().get().getDriverRating());
            entry.setVehicleRating(passengerRides.stream()
                    .filter(pr -> pr.getRide().getId() == entry.getId())
                    .findFirst().get().getVehicleRating());
            return entry;
        }).toList();
        return page;
    }

    public Page<RideHistoryDisplayDTO> getDriverRideHistory(Authentication auth, Pageable paging, String username) {
        User user = (User) auth.getPrincipal();
        if (username.isEmpty()) {
            username = user.getUsername();
        }
        else {
            if (!user.getRoles().get(0).getName().equals("ROLE_ADMIN")) {
                return null;
            }
        }
        Page<Ride> rides = rideRepository.findByDriverUsername(username, paging);
        Page<RideHistoryDisplayDTO> page = rides.map(ride -> modelMapper
                .map(ride, RideHistoryDisplayDTO.class));
        return page;
    }

    public DetailedRideHistoryPassengerDTO detailedRideHistoryPassenger(Integer rideId, Authentication authentication) {
        Optional<Ride> optRide = rideRepository.findById(rideId);
        List<PassengerRide> passengerRides = passengerRideRepository.findByRideId(rideId);
        if(optRide.isPresent()){
            DetailedRideHistoryPassengerDTO returnDTO = modelMapper.map(optRide.get(), DetailedRideHistoryPassengerDTO.class);
            for(PassengerRide passengerRide : passengerRides){
                if(passengerRide.getDriverRating() != 0)
                    returnDTO.getDriverRating().put(passengerRide.getPassenger().getUsername(), passengerRide.getDriverRating());
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

    public Boolean leaveReview(ReviewCreationDTO reviewCreationDTO, Authentication authentication) {
        Passenger passenger = (Passenger) authentication.getPrincipal();
        PassengerRide passengerRide = passengerRideRepository.
                findByRideIdAndPassengerUsername(reviewCreationDTO.getRideId(), passenger.getUsername()).orElseThrow();
        LocalDateTime rideEnd = passengerRide.getRide().getEndTime();
        if (rideEnd.plus(72, ChronoUnit.HOURS).isAfter(LocalDateTime.now())) {
            passengerRide.setComment(reviewCreationDTO.getComment());
            passengerRide.setVehicleRating(reviewCreationDTO.getVehicleRating());
            passengerRide.setDriverRating(reviewCreationDTO.getDriverRating());
            passengerRideRepository.save(passengerRide);

            Driver driver = passengerRide.getRide().getDriver();
            driver.setTotalRatingSum(driver.getTotalRatingSum() + passengerRide.getDriverRating());
            driver.setNumberOfReviews(driver.getNumberOfReviews() + 1);
            driverRepository.save(driver);
            return true;
        }
        return false;
    }

    public Date getDateFromString(String dateString){
        int startYear = Integer.parseInt(dateString.split("-")[0]);
        int startMonth = Integer.parseInt(dateString.split("-")[1]);
        int startDay = Integer.parseInt(dateString.split("-")[2]);
        return new Date(startYear-1900,startMonth,startDay);
    }

    public ReportDisplayDTO generateReportPassenger(String startDateString, String endDateString, ReportParameter reportParameter, Authentication authentication) {
        Date startDate = getDateFromString(startDateString);
        Date endDate = getDateFromString(endDateString);
        Passenger passenger = (Passenger) authentication.getPrincipal();
        List<Object[]> queryRet;
        ReportDisplayDTO reportDisplayDTO = new ReportDisplayDTO();
        switch(reportParameter){
            case MONEY_SPENT_EARNED -> {queryRet = passengerRideRepository.getPassengersMoneyReport(startDate, endDate, passenger.getUsername());
                reportDisplayDTO.setYAxisName("Money spent"); break;}
            case NUM_OF_RIDES ->  {queryRet = passengerRideRepository.getPassengerRidesReport(startDate, endDate, passenger.getUsername());
                reportDisplayDTO.setYAxisName("Number of rides");  break;}
            default -> {queryRet = passengerRideRepository.getPassengerDistanceReport(startDate, endDate, passenger.getUsername());
                reportDisplayDTO.setYAxisName("Distance traveled"); }
        }
        return generateReportDisplayDTO(queryRet, reportDisplayDTO);
    }

    public ReportDisplayDTO generateReportDriver(String startDateString, String endDateString, ReportParameter reportParameter, Authentication authentication) {
        Date startDate = getDateFromString(startDateString);
        Date endDate = getDateFromString(endDateString);
        Driver driver = (Driver) authentication.getPrincipal();
        List<Object[]> queryRet;
        ReportDisplayDTO reportDisplayDTO = new ReportDisplayDTO();
        switch(reportParameter){
            case MONEY_SPENT_EARNED -> {queryRet = rideRepository.getDriverMoneyReport(startDate, endDate, driver.getUsername());
                reportDisplayDTO.setYAxisName("Money earned"); ; break;}
            case NUM_OF_RIDES ->  {queryRet = rideRepository.getDriverRidesReport(startDate, endDate, driver.getUsername());
                reportDisplayDTO.setYAxisName("Number of rides");  break;}
            default -> {queryRet = rideRepository.getDriverDistanceReport(startDate, endDate, driver.getUsername());
                reportDisplayDTO.setYAxisName("Distance traveled");}
        }
        return generateReportDisplayDTO(queryRet, reportDisplayDTO);
    }

    public ReportDisplayDTO generateReportAdmin(String startDateString, String endDateString, ReportParameter reportParameter, String type, Authentication authentication) {
        Date startDate = getDateFromString(startDateString);
        Date endDate = getDateFromString(endDateString);
        List<Object[]> queryRet;
        ReportDisplayDTO reportDisplayDTO = new ReportDisplayDTO();
        if(Objects.equals(type, "driver")){
            switch(reportParameter){
                case MONEY_SPENT_EARNED -> {queryRet = rideRepository.getAllDriversMoneyReport(startDate, endDate);
                    reportDisplayDTO.setYAxisName("Money spent"); break;}
                case NUM_OF_RIDES ->  {queryRet = rideRepository.getAllDriversRidesReport(startDate, endDate);
                    reportDisplayDTO.setYAxisName("Number of rides"); break;}
                default -> {queryRet = rideRepository.getAllDriversDistanceReport(startDate, endDate);
                    reportDisplayDTO.setYAxisName("Distance traveled");}
            }
        }
        else{
            switch(reportParameter){
                case MONEY_SPENT_EARNED -> {queryRet = passengerRideRepository.getAllPassengersMoneyReport(startDate, endDate);
                    reportDisplayDTO.setYAxisName("Money earned"); break;}
                case NUM_OF_RIDES ->  {queryRet = passengerRideRepository.getAllPassengersRidesReport(startDate, endDate);
                    reportDisplayDTO.setYAxisName("Number of rides"); break;}
                default -> {queryRet = passengerRideRepository.getAllPassengersDistanceReport(startDate, endDate);
                    reportDisplayDTO.setYAxisName("Distance traveled");}
            }
        }
        return generateReportDisplayDTO(queryRet, reportDisplayDTO);
    }

    private ReportDisplayDTO generateReportDisplayDTO(List<Object[]> queryRet, ReportDisplayDTO reportDisplayDTO) {
        double sumY = 0;
        reportDisplayDTO.setXAxisName("date");
        for(Object[] x : queryRet){
            if(x[0] != null && x[1] != null){
                reportDisplayDTO.addXAxisValue(String.valueOf(x[0]));
                reportDisplayDTO.addYAxisValue(Double.parseDouble(String.valueOf(x[1])));
                sumY += Double.parseDouble(String.valueOf(x[1]));
            }
        }
        reportDisplayDTO.setSum(sumY);
        reportDisplayDTO.setAverage(sumY/queryRet.size());
        return reportDisplayDTO;
    }

    private void sendMessageToPassenger(String receiverUsername, String content, MessageType messageType) {
        WSMessage message = WSMessage.builder()
                .type(messageType)
                .sender("server")
                .receiver(receiverUsername)
                .content(content)
                .sentDateTime(LocalDateTime.now())
                .build();
        this.template.convertAndSendToUser(receiverUsername, "/private/passenger/ride", message);
    }

    private void sendMessageToMultiplePassengers(List<String> receiverUsernames, String message, MessageType messageType) {
        for (String receiverUsername : receiverUsernames) {
            sendMessageToPassenger(receiverUsername, message, messageType);
        }
    }

    private void sendMessageToDriver(String receiverUsername, String content, MessageType messageType) {
        WSMessage message = WSMessage.builder()
                .type(messageType)
                .sender("server")
                .receiver(receiverUsername)
                .content(content)
                .sentDateTime(LocalDateTime.now())
                .build();
        this.template.convertAndSendToUser(receiverUsername, "/private/driver/ride", message);
    }

    private void notifyPassengersAboutReservation(List<String> receiverUsernames, int minutesLeft) {
        for (String receiverUsername : receiverUsernames) {
            sendDisappearingMessage(receiverUsername,
                    "The ride you scheduled should start in " + minutesLeft + " minutes.");
        }
    }

    private void sendDisappearingMessage(String receiverUsername, String content) {
        WSMessage message = WSMessage.builder()
                .type(MessageType.DISAPPEARING)
                .sender("server")
                .receiver(receiverUsername)
                .content(content)
                .sentDateTime(LocalDateTime.now())
                .build();
        this.template.convertAndSendToUser(receiverUsername, "/private/ride/disappearing", message);
    }

    private void sendRefreshMessageToDriverAndAllPassengers(Ride ride) {
        List<PassengerRide> passengerRides = passengerRideRepository.findByRide(ride);
        sendRefreshMessage(ride.getDriver().getUsername());
        sendRefreshMessageToMultipleUsers(
                passengerRides.stream().map(pr -> pr.getPassenger().getUsername()).toList());
    }

    private void sendRefreshMessage(String receiverUsername) {
        WSMessage message = WSMessage.builder()
                .type(MessageType.RIDE_UPDATE)
                .sender("server")
                .receiver(receiverUsername)
                .content("REFRESH")
                .sentDateTime(LocalDateTime.now())
                .build();
        this.template.convertAndSendToUser(receiverUsername, "/private/ride/refresh", message);
    }

    private void sendRefreshMessageToMultipleUsers(List<String> receiverUsernames) {
        for (String receiverUsername : receiverUsernames) {
            sendRefreshMessage(receiverUsername);
        }
    }
}
