package com.example.springbackend.service;

import com.example.springbackend.dto.creation.*;
import com.example.springbackend.dto.display.*;
import com.example.springbackend.exception.*;
import com.example.springbackend.model.*;
import com.example.springbackend.model.helpClasses.ReportParameter;
import com.example.springbackend.repository.*;
import com.example.springbackend.util.RideUtils;
import com.example.springbackend.websocket.MessageType;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    RideUtils rideUtils;
    @Autowired
    SimulatorService simulatorService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public RideSimpleDisplayDTO orderBasicRide(BasicRideCreationDTO dto, Authentication auth) {
        Passenger passenger = (Passenger) auth.getPrincipal();
        VehicleType vehicleType = vehicleTypeRepository.findByName(dto.getVehicleType()).orElseThrow();

        Optional<PassengerRide> currentPassengerRide = passengerRideRepository.getCurrentPassengerRide(passenger);
        if (currentPassengerRide.isPresent()) {
            throw new PassengerAlreadyHasAnActiveRideException();
        }

        int price = rideUtils.calculateRidePrice(dto, vehicleType);
        if (passenger.getTokenBalance() < price) {
            throw new InsufficientFundsException();
        }

        Driver driver = null;
        Ride ride = rideUtils.createBasicRide(dto, price, null);
        PassengerRide passengerRide = rideUtils.createPassengerRide(passenger, ride);

        if (dto.getDelayInMinutes() == 0) {
            driver = rideUtils.findDriver(ride);
            if (driver == null) {
                ride.setStatus(RideStatus.CANCELLED);
                rideRepository.save(ride);
                throw new AdequateDriverNotFoundException();
            } else {
                rideUtils.linkDriverAndRide(driver, ride);
                passenger.setTokenBalance(passenger.getTokenBalance() - price);
                passengerRepository.save(passenger);
                rideUtils.sendRefreshMessage(driver.getUsername());
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
            rideUtils.handleNotificationsAndProcessReservations(ride, passengerRides);
        }

        RideSimpleDisplayDTO rideDisplayDTO = rideUtils.createBasicRideSimpleDisplayDTO(passengerRide, driver);
        return rideDisplayDTO;
    }

    public RideSimpleDisplayDTO orderSplitFareRide(SplitFareRideCreationDTO dto, Authentication auth) {
        Passenger passenger = (Passenger) auth.getPrincipal();
        VehicleType vehicleType = vehicleTypeRepository.findByName(dto.getVehicleType()).orElseThrow();

        int price = rideUtils.calculateRidePrice(dto, vehicleType);
        dto.getUsersToPay().add(passenger.getUsername());
        int fare = (int) Math.ceil(price/dto.getUsersToPay().size());

        rideUtils.checkIfSplitFareRideIsValid(dto, passenger, fare, vehicleType);

        Ride ride = rideUtils.createSplitFareRide(dto, price);
        passenger.setTokenBalance(passenger.getTokenBalance() - fare);
        passengerRepository.save(passenger);
        rideUtils.createPassengerRideForUsers(dto, ride, fare, passenger);

        dto.getUsersToPay().stream().forEach(username -> {
            if (!username.equals(passenger.getUsername()))
                rideUtils.sendRefreshMessage(username);
        });

        scheduleExecution(() -> processSplitFareRide(dto, ride.getId()), 30, TimeUnit.SECONDS);

        PassengerRide passengerRide = passengerRideRepository
                .findByRideAndPassengerUsername(ride, passenger.getUsername()).get();
        RideSimpleDisplayDTO rideDisplayDTO = rideUtils.createBasicRideSimpleDisplayDTO(passengerRide, null);
        return rideDisplayDTO;
    }

    public void processSplitFareRide(SplitFareRideCreationDTO dto, Integer rideId) {
        Ride ride = rideRepository.findById(rideId).get();
        if (ride.getStatus() != RideStatus.CANCELLED && !ride.getPassengersConfirmed()) {
            for (String username : dto.getUsersToPay()) {
                rideUtils.sendMessageToPassenger(username,
                        "The ride is cancelled because one of the passengers did not respond to the invitation.",
                        MessageType.RIDE_ERROR);
            }
            List<PassengerRide> passengerRides = passengerRideRepository.findByRide(ride);
            rideUtils.refundPassengers(passengerRides);
            ride.setStatus(RideStatus.CANCELLED);
            rideRepository.save(ride);
        }
    }

    public Boolean confirmRide(RideIdDTO dto, Authentication auth) {
        Ride ride = rideRepository.findById(dto.getRideId()).orElseThrow();
        Passenger passenger = (Passenger) auth.getPrincipal();
        List<PassengerRide> passengerRides = passengerRideRepository.findByRide(ride);
        List<String> usersToPay = passengerRides.stream().map(pr -> pr.getPassenger().getUsername()).toList();

        PassengerRide currentPR = passengerRideRepository
                .findByRideAndPassengerUsername(ride, passenger.getUsername()).orElseThrow();

        if (passenger.getTokenBalance() < currentPR.getFare()) {
            for (String username : usersToPay) {
                rideUtils.sendMessageToPassenger(username,
                        "Ride is cancelled due to insufficient funds.",
                        MessageType.RIDE_ERROR);
            }
            ride.setStatus(RideStatus.CANCELLED);
            rideRepository.save(ride);
            rideUtils.refundPassengers(passengerRides);
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
                break;
            }
        }
        if (fullyPaid) {
            ride.setPassengersConfirmed(true);
            ride.setStatus(RideStatus.RESERVED);
            rideRepository.save(ride);
            if (ride.getDelayInMinutes() == 0) {
                rideUtils.processReservation(ride, passengerRides);
            } else {
                rideUtils.sendRefreshMessageToMultipleUsers(usersToPay);
                rideUtils.handleNotificationsAndProcessReservations(ride, passengerRides);
            }
        }
        return true;
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
                    rideUtils.sendMessageToPassenger(ridePassenger.getUsername(),
                            "A passenger has rejected the ride.",
                            MessageType.RIDE_ERROR);
            }
            rideUtils.refundPassengers(passengerRides);
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
            rideUtils.sendRefreshMessageToDriverAndAllPassengers(ride);
            return true;
        } else {
            throw new RideDoesNotBelongToPassengerException();
        }
    }

    public Boolean beginRide(RideIdDTO dto, Authentication auth) {
        Driver driver = (Driver) auth.getPrincipal();
        Ride ride = rideRepository.findById(dto.getRideId()).orElseThrow();
        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartTime(LocalDateTime.now());
        rideRepository.save(ride);
        rideUtils.directDriverToLocation(driver, ride.getRoute().getWaypoints().get(1));
        rideUtils.sendRefreshMessageToDriverAndAllPassengers(ride);

        scheduleExecution(() -> rideUtils.markArrivedAtDestination(ride.getId(), 1),
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
        rideUtils.sendMessageToMultiplePassengers(passengerRides.stream().map(pr -> pr.getPassenger().getUsername()).toList(),
                "", MessageType.RIDE_COMPLETE);

        for (PassengerRide pr : passengerRides) {
            Passenger passenger = pr.getPassenger();
            passenger.setRidesCompleted(passenger.getRidesCompleted() + 1);
            passenger.setDistanceTravelled(pr.getPassenger().getDistanceTravelled() + ride.getDistance());
            passengerRepository.save(passenger);
        }

        rideUtils.updateCurrentDriverRide(driver);
        rideUtils.sendRefreshMessage(driver.getUsername());
        return true;
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
            rideUtils.sendMessageToDriver(ride.getDriver().getUsername(),
                    "Your rejection reason was deemed invalid.",
                    MessageType.RIDE_ERROR);
            return true;
        }

        ride.setStatus(RideStatus.CANCELLED);
        rideRepository.save(ride);

        Driver driver = ride.getDriver();

        if (driver.getNextRide() != null) {
            Ride nextRide = rideRepository.findById(driver.getNextRide().getId()).get();
            nextRide.setStatus(RideStatus.CANCELLED);
            rideRepository.save(nextRide);
            List<PassengerRide> nextRidePassengerRides = passengerRideRepository.findByRide(nextRide);
            rideUtils.handleRejectedRidePassengers(nextRidePassengerRides);
        }

        driver.setActive(false);
        driver.setCurrentRide(null);
        driver.setNextRide(null);
        driverRepository.save(driver);

        Vehicle vehicle = driver.getVehicle();
        vehicle.setCurrentCoordinates(vehicle.getNextCoordinates());
        vehicle.setExpectedTripTime(0);
        vehicle.setRideActive(false);
        vehicle.setCoordinatesChangedAt(LocalDateTime.now());
        vehicleRepository.save(vehicle);

        List<PassengerRide> passengerRides = passengerRideRepository.findByRide(ride);
        rideUtils.handleRejectedRidePassengers(passengerRides);

        rideUtils.sendMessageToDriver(ride.getDriver().getUsername(),
                "Your rejection is accepted and your rides are cancelled.",
                MessageType.RIDE_UPDATE);

        return true;
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
        System.out.println(startYear);
        System.out.println(startMonth);
        System.out.println(startDay);
        return new Date(startYear-1900,startMonth - 1,startDay);
    }

    public ReportDisplayDTO generateReportPassenger(String startDateString, String endDateString, ReportParameter reportParameter, Authentication authentication) {
        Date startDate = getDateFromString(startDateString);
        Date endDate = getDateFromString(endDateString);
        Passenger passenger = (Passenger) authentication.getPrincipal();
        List<Object[]> queryRet;
        ReportDisplayDTO reportDisplayDTO = new ReportDisplayDTO();
        System.out.println(startDateString);
        System.out.println(endDateString);
        System.out.println(startDate);
        System.out.println(endDate);
        System.out.println(reportParameter);
        switch(reportParameter){
            case MONEY_SPENT_EARNED -> {queryRet = passengerRideRepository.getPassengersMoneyReport(startDate, endDate, passenger.getUsername());
                reportDisplayDTO.setYAxisName("Money spent"); break;}
            case NUM_OF_RIDES ->  {queryRet = passengerRideRepository.getPassengerRidesReport(startDate, endDate, passenger.getUsername());
                reportDisplayDTO.setYAxisName("Number of rides");  break;}
            default -> {queryRet = passengerRideRepository.getPassengerDistanceReport(startDate, endDate, passenger.getUsername());
                reportDisplayDTO.setYAxisName("Distance traveled"); }
        }
        return rideUtils.generateReportDisplayDTO(queryRet, reportDisplayDTO);
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
        return rideUtils.generateReportDisplayDTO(queryRet, reportDisplayDTO);
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
        return rideUtils.generateReportDisplayDTO(queryRet, reportDisplayDTO);
    }

    public void scheduleExecution(Runnable runnable, long delay, TimeUnit timeUnit) {
        scheduler.schedule(runnable, delay, timeUnit);
    }
}
