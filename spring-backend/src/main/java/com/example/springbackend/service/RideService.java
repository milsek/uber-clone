package com.example.springbackend.service;

import com.example.springbackend.dto.creation.*;
import com.example.springbackend.dto.display.*;
import com.example.springbackend.exception.AdequateDriverNotFoundException;
import com.example.springbackend.exception.InsufficientFundsException;
import com.example.springbackend.exception.LinkedPassengersNotAllDistinctException;
import com.example.springbackend.exception.UserDoesNotExistException;
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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
    VehicleRepository vehicleRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    UserService userService;
    private final SimpMessagingTemplate template;
    @Autowired
    RideService(SimpMessagingTemplate template) {
        this.template = template;
    }

    public Boolean orderSplitFareRide(SplitFareRideCreationDTO dto, Authentication auth) {
        Passenger passenger = (Passenger) auth.getPrincipal();
        VehicleType vehicleType = vehicleTypeRepository.findByName(dto.getVehicleType()).orElseThrow();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        int price = calculateRidePrice(dto, vehicleType);
        dto.getUsersToPay().add(passenger.getUsername());
        if (dto.getUsersToPay().stream().distinct().count() != dto.getUsersToPay().size()) {
            throw new LinkedPassengersNotAllDistinctException();
        }
        dto.getUsersToPay().stream().forEach(email -> {
            if (!userService.userExists(email)) {
                throw new UserDoesNotExistException("A co-passenger's email does not exist in the system.");
            }
        });
        int fare = (int) Math.ceil(price/dto.getUsersToPay().size());
        Ride ride = createSplitFareRide(dto, price);
        if (passenger.getTokenBalance() < fare) {
            throw new InsufficientFundsException();
        }
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
        if(fullyPaid) {
            ride.setPassengersConfirmed(true);
            BasicRideCreationDTO basicRideCreationDTO = modelMapper.map(ride, BasicRideCreationDTO.class);
            basicRideCreationDTO.setVehicleType(ride.getVehicleType());

            Driver driver = findDriver(basicRideCreationDTO);
            ride.setDriver(driver);
            rideRepository.save(ride);

            if (driver == null) {
                for (String username : usersToPay) {
                    sendMessageToPassenger(username, "Adequate driver was not found.", MessageType.RIDE_ERROR);
                }
                ride.setStatus(RideStatus.CANCELLED);
                rideRepository.save(ride);
                refundPassengers(passengerRides);
                throw new AdequateDriverNotFoundException();
            } else {
                sendRefreshMessage(driver.getUsername());
                for (String username : usersToPay) {
                    sendRefreshMessage(username);
                }
                if (driver.getCurrentRide() == null) {
                    driver.setCurrentRide(ride);
                } else {
                    driver.setNextRide(ride);
                }
                driverRepository.save(driver);
                ride.setStatus(RideStatus.DRIVER_ARRIVING);
                rideRepository.save(ride);
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

    private void refundPassengers(List<PassengerRide> passengerRides) {
        for (PassengerRide passengerRide : passengerRides) {
            Passenger ridePassenger = passengerRide.getPassenger();
            if (passengerRide.isAgreed()) {
                ridePassenger.setTokenBalance(ridePassenger.getTokenBalance() + passengerRide.getFare());
                passengerRepository.save(ridePassenger);
            }
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
        driver.setCurrentRide(driver.getNextRide());
        driver.setNextRide(null);
        driverRepository.save(driver);

        Vehicle vehicle = driver.getVehicle();
        vehicle.setCurrentCoordinates(vehicle.getNextCoordinates());
        if (driver.getCurrentRide() != null) {
            List<Coordinates> waypoints = driver.getCurrentRide().getActualRoute().getWaypoints();
            vehicle.setNextCoordinates(waypoints.get(0));
        }
        vehicleRepository.save(vehicle);

        List<PassengerRide> passengerRides = passengerRideRepository.findByRide(ride);

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

        sendMessageToDriver(ride.getDriver().getUsername(),
                "Your rejection is accepted. The ride will be cancelled.",
                MessageType.RIDE_UPDATE);

        return true;
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
        Ride ride = createBasicRide(dto, price, driver);
        PassengerRide passengerRide = createPassengerRide(passenger, ride);

        sendRefreshMessage(driver.getUsername()
        );
        RideSimpleDisplayDTO rideDisplayDTO = createBasicRideSimpleDisplayDTO(passengerRide, driver);

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
        ride.setDriverRejectionReason(null);
        ride.setStatus(RideStatus.DRIVER_ARRIVING);
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
        ride.setDriverRejectionReason(null);
        ride.setStatus(RideStatus.PENDING_CONFIRMATION);
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

    public RideSimpleDisplayDTO createBasicRideSimpleDisplayDTO(PassengerRide pr, Driver driver) {
        Ride ride = pr.getRide();
        RideSimpleDisplayDTO rideDisplayDTO = modelMapper.map(ride, RideSimpleDisplayDTO.class);
        rideDisplayDTO.setDriver(driver != null ? modelMapper.map(driver, DriverSimpleDisplayDTO.class) : null);
        rideDisplayDTO.setAllConfirmed(ride.getPassengersConfirmed());
        rideDisplayDTO.setPassengerConfirmed(pr.isAgreed());
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

    public RouteDisplayDTO createRouteDisplayDtoFromRoute(Route route) {
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

    public Date getDateFromString(String dateString){
        int startYear = Integer.parseInt(dateString.split("-")[2]);
        int startMonth = Integer.parseInt(dateString.split("-")[1]);
        int startDay = Integer.parseInt(dateString.split("-")[0]);
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
}
