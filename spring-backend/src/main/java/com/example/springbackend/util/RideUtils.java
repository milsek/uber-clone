package com.example.springbackend.util;

import com.example.springbackend.dto.creation.BasicRideCreationDTO;
import com.example.springbackend.dto.creation.RouteCreationDTO;
import com.example.springbackend.dto.creation.SplitFareRideCreationDTO;
import com.example.springbackend.dto.display.*;
import com.example.springbackend.exception.*;
import com.example.springbackend.model.*;
import com.example.springbackend.model.helpClasses.Coordinates;
import com.example.springbackend.repository.*;
import com.example.springbackend.service.PassengerService;
import com.example.springbackend.service.SimulatorService;
import com.example.springbackend.service.UserService;
import com.example.springbackend.websocket.MessageType;
import com.example.springbackend.websocket.WSMessage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RideUtils {

    @Autowired
    RideRepository rideRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    PassengerRepository passengerRepository;
    @Autowired
    PassengerRideRepository passengerRideRepository;
    @Autowired
    RouteRepository routeRepository;
    @Autowired
    VehicleRepository vehicleRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    UserService userService;
    @Autowired
    SimulatorService simulatorService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final SimpMessagingTemplate template;
    @Autowired
    RideUtils(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void handleNotificationsAndProcessReservations(Ride ride, List<PassengerRide> passengerRides) {
        List<String> passengerUsernames = passengerRides.stream().map(pr ->
                pr.getPassenger().getUsername()).toList();
        scheduleExecution(() -> notifyPassengersAboutReservation(passengerUsernames, 15),
                ride.getDelayInMinutes() - 15, TimeUnit.SECONDS);  // should be minutes in production
        scheduleExecution(() -> notifyPassengersAboutReservation(passengerUsernames, 10),
                ride.getDelayInMinutes() - 10, TimeUnit.SECONDS);  // should be minutes in production
        scheduleExecution(() -> notifyPassengersAboutReservation(passengerUsernames, 5),
                ride.getDelayInMinutes() - 5, TimeUnit.SECONDS);  // should be minutes in production
        scheduleExecution(() -> processReservation(ride, passengerRides),
                ride.getDelayInMinutes(), TimeUnit.SECONDS);  // should be minutes in production
    }

    public void processReservation(Ride ride, List<PassengerRide> passengerRides) {
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

    public void linkDriverAndRide(Driver driver, Ride ride) {
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

    public void directDriverToCurrentRideStart(Driver driver, Ride ride) {
        directDriverToLocation(driver, ride.getRoute().getWaypoints().get(0));
        scheduleExecution(() -> markDriverArrived(ride.getId()),
                driver.getVehicle().getExpectedTripTime(), TimeUnit.SECONDS);
    }

    public void markDriverArrived(Integer rideId) {
        Ride ride = rideRepository.findById(rideId).get();
        if (ride.getStatus() == RideStatus.CANCELLED) return;
        ride.setStatus(RideStatus.DRIVER_ARRIVED);
        rideRepository.save(ride);
        sendRefreshMessageToDriverAndAllPassengers(ride);
    }

    public void markArrivedAtDestination(Integer rideId, int waypointIndex) {
        Ride ride = rideRepository.findById(rideId).get();
        if (ride.getStatus() == RideStatus.CANCELLED) return;
        if (ride.getRoute().getWaypoints().size() > waypointIndex + 1) {
            directDriverToLocation(ride.getDriver(), ride.getRoute().getWaypoints().get(waypointIndex + 1));
            scheduleExecution(() -> markArrivedAtDestination(ride.getId(), waypointIndex + 1),
                    ride.getDriver().getVehicle().getExpectedTripTime(), TimeUnit.SECONDS);
        } else {
            ride.setStatus(RideStatus.ARRIVED_AT_DESTINATION);
            rideRepository.save(ride);
        }
        sendRefreshMessageToDriverAndAllPassengers(ride);
    }

    public void directDriverToLocation(Driver driver, Coordinates coordinates) {
        Driver driverActual = driverRepository.findByUsername(driver.getUsername()).get();
        if (driverActual.getCurrentRide() != null || driverActual.getNextRide() != null) {
            Vehicle vehicle = driverActual.getVehicle();
            vehicle.setNextCoordinates(coordinates);
            vehicle.setRideActive(true);
            vehicle.setCoordinatesChangedAt(LocalDateTime.now());
            long estimatedTime = simulatorService.getEstimatedTime(vehicle);
            vehicle.setExpectedTripTime(estimatedTime);
            vehicleRepository.save(vehicle);
            scheduleExecution(() -> simulatorService.arriveAtLocation(vehicle, true),
                    estimatedTime, TimeUnit.SECONDS);
        }
    }

    public void createPassengerRideForUsers(SplitFareRideCreationDTO dto, Ride ride, int fare, Passenger passenger) {
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

    public void createSplitFarePassengerRide(Passenger passenger, Ride ride, int fare, boolean agreed) {
        PassengerRide passengerRide = new PassengerRide();
        passengerRide.setPassenger(passenger);
        passengerRide.setRide(ride);
        passengerRide.setFare(fare);
        passengerRide.setAgreed(agreed);
        passengerRideRepository.save(passengerRide);
    }

    public void refundPassengers(List<PassengerRide> passengerRides) {
        for (PassengerRide passengerRide : passengerRides) {
            Passenger ridePassenger = passengerRide.getPassenger();
            if (passengerRide.isAgreed()) {
                ridePassenger.setTokenBalance(ridePassenger.getTokenBalance() + passengerRide.getFare());
                passengerRepository.save(ridePassenger);
            }
        }
    }

    public void checkIfSplitFareRideIsValid(SplitFareRideCreationDTO dto, Passenger passenger, int fare, VehicleType vehicleType) {
        dto.getUsersToPay().stream().forEach(email -> {
            if (!passengerRepository.findByEmail(email).isPresent()) {
                throw new UserDoesNotExistException("A co-passenger's email does not exist in the system.");
            }
        });
        if (dto.getUsersToPay().stream().distinct().count() != dto.getUsersToPay().size()) {
            throw new LinkedPassengersNotAllDistinctException();
        }
        if (dto.getUsersToPay().size() > vehicleType.getSeats()) {
            throw new TooManyPassengersException();
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

    public void updateCurrentDriverRide(Driver driver) {
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

    public void handleRejectedRidePassengers(List<PassengerRide> passengerRides) {
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

    public Driver findDriver(Ride ride) {
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

    public PassengerRide createPassengerRide(Passenger passenger, Ride ride) {
        PassengerRide passengerRide = new PassengerRide();
        passengerRide.setPassenger(passenger);
        passengerRide.setRide(ride);
        passengerRide.setFare(ride.getPrice());
        passengerRide.setAgreed(true);
        passengerRideRepository.save(passengerRide);
        return passengerRide;
    }

    public Ride createBasicRide(BasicRideCreationDTO dto, int price, Driver driver) {
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
    public Ride createSplitFareRide(BasicRideCreationDTO dto, int price) {
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

    public int calculateRidePrice(BasicRideCreationDTO dto, VehicleType vehicleType) {
        return (int) Math.round(vehicleType.getPrice() + dto.getDistance() * 120);
    }

    public Route createRouteFromDto(RouteCreationDTO dto) {
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

    public RideSimpleDisplayDTO createBasicRideSimpleDisplayDTO(PassengerRide pr, Driver driver) {
        Ride ride = pr.getRide();
        RideSimpleDisplayDTO rideDisplayDTO = modelMapper.map(ride, RideSimpleDisplayDTO.class);
        rideDisplayDTO.setDriver(driver != null ? modelMapper.map(driver, DriverSimpleDisplayDTO.class) : null);
        rideDisplayDTO.setAllConfirmed(ride.getPassengersConfirmed());
        rideDisplayDTO.setPassengerConfirmed(pr.isAgreed());
        rideDisplayDTO.setRoute(createRouteDisplayDtoFromRoute(ride.getRoute()));
        return rideDisplayDTO;
    }

    public ReportDisplayDTO generateReportDisplayDTO(List<Object[]> queryRet, ReportDisplayDTO reportDisplayDTO) {
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

    public void sendMessageToPassenger(String receiverUsername, String content, MessageType messageType) {
        WSMessage message = WSMessage.builder()
                .type(messageType)
                .sender("server")
                .receiver(receiverUsername)
                .content(content)
                .sentDateTime(LocalDateTime.now())
                .build();
        this.template.convertAndSendToUser(receiverUsername, "/private/passenger/ride", message);
    }

    public void sendMessageToMultiplePassengers(List<String> receiverUsernames, String message, MessageType messageType) {
        for (String receiverUsername : receiverUsernames) {
            sendMessageToPassenger(receiverUsername, message, messageType);
        }
    }

    public void sendMessageToDriver(String receiverUsername, String content, MessageType messageType) {
        WSMessage message = WSMessage.builder()
                .type(messageType)
                .sender("server")
                .receiver(receiverUsername)
                .content(content)
                .sentDateTime(LocalDateTime.now())
                .build();
        this.template.convertAndSendToUser(receiverUsername, "/private/driver/ride", message);
    }

    public void notifyPassengersAboutReservation(List<String> receiverUsernames, int minutesLeft) {
        for (String receiverUsername : receiverUsernames) {
            sendDisappearingMessage(receiverUsername,
                    "The ride you scheduled should start in " + minutesLeft + " minutes.");
        }
    }

    public void sendDisappearingMessage(String receiverUsername, String content) {
        WSMessage message = WSMessage.builder()
                .type(MessageType.DISAPPEARING)
                .sender("server")
                .receiver(receiverUsername)
                .content(content)
                .sentDateTime(LocalDateTime.now())
                .build();
        this.template.convertAndSendToUser(receiverUsername, "/private/ride/disappearing", message);
    }

    public void sendRefreshMessageToDriverAndAllPassengers(Ride ride) {
        List<PassengerRide> passengerRides = passengerRideRepository.findByRide(ride);
        sendRefreshMessage(ride.getDriver().getUsername());
        sendRefreshMessageToMultipleUsers(
                passengerRides.stream().map(pr -> pr.getPassenger().getUsername()).toList());
    }

    public void sendRefreshMessage(String receiverUsername) {
        WSMessage message = WSMessage.builder()
                .type(MessageType.RIDE_UPDATE)
                .sender("server")
                .receiver(receiverUsername)
                .content("REFRESH")
                .sentDateTime(LocalDateTime.now())
                .build();
        this.template.convertAndSendToUser(receiverUsername, "/private/ride/refresh", message);
    }

    public void sendRefreshMessageToMultipleUsers(List<String> receiverUsernames) {
        for (String receiverUsername : receiverUsernames) {
            sendRefreshMessage(receiverUsername);
        }
    }

    public void scheduleExecution(Runnable runnable, long delay, TimeUnit timeUnit) {
        scheduler.schedule(runnable, delay, timeUnit);
    }

}
