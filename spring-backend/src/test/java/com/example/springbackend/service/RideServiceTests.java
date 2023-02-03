package com.example.springbackend.service;

import com.example.springbackend.dto.creation.*;
import com.example.springbackend.dto.display.RideSimpleDisplayDTO;
import com.example.springbackend.exception.*;
import com.example.springbackend.model.*;
import com.example.springbackend.model.helpClasses.Coordinates;
import com.example.springbackend.repository.*;
import com.example.springbackend.util.RideUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@SpringBootTest(classes = TestConfig.class)
@ExtendWith(MockitoExtension.class)
public class RideServiceTests {
    @Mock
    private VehicleTypeRepository vehicleTypeRepositoryMock;
    @Mock
    private VehicleRepository vehicleRepositoryMock;
    @Mock
    private PassengerRideRepository passengerRideRepositoryMock;
    @Mock
    private PassengerRepository passengerRepositoryMock;
    @Mock
    private RideRepository rideRepositoryMock;
    @Mock
    private DriverRepository driverRepositoryMock;
    @Mock
    private RideUtils rideUtilsMock;

    @Mock
    private Passenger passengerMock;
    @Mock
    private PassengerRide passengerRideMock;
    @Mock
    private Vehicle vehicleMock;
    @Mock
    private VehicleType vehicleTypeMock;
    @Mock
    private Authentication authenticationMock;
    @Mock
    private Driver driverMock;
    @Mock
    private Ride rideMock;

    @InjectMocks
    private RideService rideService;

    @BeforeEach
    void beforeEach() {
        reset();
    }

    // basic ride
    @Test
    void Throw_exception_when_vehicle_type_does_not_exist_when_ordering_a_basic_ride() {
        BasicRideCreationDTO dto = mock(BasicRideCreationDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);
        when(dto.getVehicleType()).thenReturn(null);
        assertThrows(
                NoSuchElementException.class,
                () -> rideService.orderBasicRide(dto, authentication),
                "Expected orderBasicRide() to throw NoSuchElementException, but it didn't"
        );
    }

    @Test
    void Throw_exception_when_current_passenger_ride_exists_while_ordering_a_basic_ride() {
        BasicRideCreationDTO dto = mock(BasicRideCreationDTO.class);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);

        when(dto.getVehicleType()).thenReturn("COUPE");
        when(vehicleTypeRepositoryMock.findByName("COUPE"))
                .thenReturn(Optional.of(vehicleTypeMock));
        when(passengerRideRepositoryMock.getCurrentPassengerRide(passengerMock))
                .thenReturn(Optional.of(passengerRideMock));

        assertThrows(
                PassengerAlreadyHasAnActiveRideException.class,
                () -> rideService.orderBasicRide(dto, authentication),
                "Expected orderBasicRide() to throw PassengerAlreadyHasAnActiveRideException, but it didn't"
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 100, 1000, 1299})
    void Throw_exception_when_passenger_lacks_tokens_for_a_basic_ride(int passengerTokenBalance) {
        BasicRideCreationDTO dto = mock(BasicRideCreationDTO.class);

        when(authenticationMock.getPrincipal()).thenReturn(passengerMock);

        when(dto.getVehicleType()).thenReturn("COUPE");
        when(vehicleTypeRepositoryMock.findByName("COUPE"))
                .thenReturn(Optional.of(vehicleTypeMock));
        when(passengerRideRepositoryMock.getCurrentPassengerRide(passengerMock))
                .thenReturn(Optional.empty());

        when(rideUtilsMock.calculateRidePrice(dto, vehicleTypeMock)).thenReturn(1300);
        when(passengerMock.getTokenBalance()).thenReturn(passengerTokenBalance);

        assertThrows(
                InsufficientFundsException.class,
                () -> rideService.orderBasicRide(dto, authenticationMock),
                "Expected orderBasicRide() to throw InsufficientFundsException, but it didn't"
        );
    }

    @Test
    void Throw_exception_when_driver_is_not_found_for_an_immediate_basic_ride() {
        BasicRideCreationDTO dto = mock(BasicRideCreationDTO.class);

        when(authenticationMock.getPrincipal()).thenReturn(passengerMock);

        when(dto.getVehicleType()).thenReturn("COUPE");
        when(vehicleTypeRepositoryMock.findByName("COUPE"))
                .thenReturn(Optional.of(vehicleTypeMock));
        when(passengerRideRepositoryMock.getCurrentPassengerRide(passengerMock))
                .thenReturn(Optional.empty());

        when(rideUtilsMock.calculateRidePrice(any(), any())).thenReturn(1300);
        when(passengerMock.getTokenBalance()).thenReturn(1300);

        when(rideUtilsMock.createBasicRide(any(), anyInt(), any())).thenReturn(rideMock);
        when(rideUtilsMock.createPassengerRide(passengerMock, rideMock)).thenReturn(passengerRideMock);
        when(dto.getDelayInMinutes()).thenReturn(0);

        when(rideUtilsMock.findDriver(rideMock)).thenReturn(null);
        doNothing().when(rideMock).setStatus(RideStatus.CANCELLED);
        when(rideRepositoryMock.save(rideMock)).thenReturn(rideMock);

        assertThrows(
                AdequateDriverNotFoundException.class,
                () -> rideService.orderBasicRide(dto, authenticationMock),
                "Expected orderBasicRide() to throw AdequateDriverNotFoundException, but it didn't"
        );
        verify(rideMock).setStatus(RideStatus.CANCELLED);
        verify(rideRepositoryMock).save(rideMock);
    }

    @Test
    void Return_object_after_a_successful_creation_of_an_immediate_basic_ride() {
        BasicRideCreationDTO dto = mock(BasicRideCreationDTO.class);

        when(authenticationMock.getPrincipal()).thenReturn(passengerMock);

        when(dto.getVehicleType()).thenReturn("COUPE");
        when(vehicleTypeRepositoryMock.findByName("COUPE"))
                .thenReturn(Optional.of(vehicleTypeMock));
        when(passengerRideRepositoryMock.getCurrentPassengerRide(passengerMock))
                .thenReturn(Optional.empty());

        when(rideUtilsMock.calculateRidePrice(any(), any())).thenReturn(1300);
        when(passengerMock.getTokenBalance()).thenReturn(1300);

        when(rideUtilsMock.createBasicRide(any(), anyInt(), any())).thenReturn(rideMock);
        when(rideUtilsMock.createPassengerRide(passengerMock, rideMock)).thenReturn(passengerRideMock);
        when(dto.getDelayInMinutes()).thenReturn(0);

        when(rideUtilsMock.findDriver(rideMock)).thenReturn(driverMock);
        doNothing().when(rideUtilsMock).linkDriverAndRide(driverMock, rideMock);
        when(passengerRepositoryMock.save(any())).thenReturn(passengerMock);
        doNothing().when(passengerMock).setTokenBalance(anyInt());

        RideSimpleDisplayDTO rideSimpleDisplayDTOMock = mock(RideSimpleDisplayDTO.class);
        when(rideUtilsMock.createBasicRideSimpleDisplayDTO(passengerRideMock, driverMock)).thenReturn(rideSimpleDisplayDTOMock);

        assertEquals(rideService.orderBasicRide(dto, authenticationMock), rideSimpleDisplayDTOMock);
        verify(rideUtilsMock).linkDriverAndRide(driverMock, rideMock);
        verify(passengerMock).setTokenBalance(anyInt());
        verify(passengerRepositoryMock).save(passengerMock);
        verify(rideUtilsMock).sendRefreshMessage(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 1, 10, 19})
    void Throw_exception_when_delay_is_shorter_than_20_for_a_scheduled_basic_ride(int delayInMinutes) {
        BasicRideCreationDTO dto = mock(BasicRideCreationDTO.class);

        when(authenticationMock.getPrincipal()).thenReturn(passengerMock);

        when(dto.getVehicleType()).thenReturn("COUPE");
        when(vehicleTypeRepositoryMock.findByName("COUPE"))
                .thenReturn(Optional.of(vehicleTypeMock));
        when(passengerRideRepositoryMock.getCurrentPassengerRide(passengerMock))
                .thenReturn(Optional.empty());

        when(rideUtilsMock.calculateRidePrice(dto, vehicleTypeMock)).thenReturn(1300);
        when(passengerMock.getTokenBalance()).thenReturn(1300);

        when(rideUtilsMock.createBasicRide(any(), anyInt(), any())).thenReturn(rideMock);
        when(rideUtilsMock.createPassengerRide(passengerMock, rideMock)).thenReturn(passengerRideMock);
        when(dto.getDelayInMinutes()).thenReturn(delayInMinutes);

        doNothing().when(rideMock).setStatus(RideStatus.CANCELLED);
        when(rideRepositoryMock.save(rideMock)).thenReturn(rideMock);

        assertThrows(
                ReservationTooSoonException.class,
                () -> rideService.orderBasicRide(dto, authenticationMock),
                "Expected orderBasicRide() to throw ReservationTooSoonException, but it didn't"
        );
        verify(rideMock).setStatus(RideStatus.CANCELLED);
        verify(rideRepositoryMock).save(rideMock);
    }

    @Test
    void Return_object_after_a_successful_creation_of_a_scheduled_basic_ride() {
        BasicRideCreationDTO dto = mock(BasicRideCreationDTO.class);

        when(authenticationMock.getPrincipal()).thenReturn(passengerMock);

        when(dto.getVehicleType()).thenReturn("COUPE");
        when(vehicleTypeRepositoryMock.findByName("COUPE"))
                .thenReturn(Optional.of(vehicleTypeMock));
        when(passengerRideRepositoryMock.getCurrentPassengerRide(passengerMock))
                .thenReturn(Optional.empty());

        when(rideUtilsMock.calculateRidePrice(any(), any())).thenReturn(1300);
        when(passengerMock.getTokenBalance()).thenReturn(1300);

        when(rideUtilsMock.createBasicRide(any(), anyInt(), any())).thenReturn(rideMock);
        when(rideUtilsMock.createPassengerRide(passengerMock, rideMock)).thenReturn(passengerRideMock);
        when(dto.getDelayInMinutes()).thenReturn(20);

        doNothing().when(passengerMock).setTokenBalance(anyInt());
        when(passengerRepositoryMock.save(passengerMock)).thenReturn(passengerMock);
        doNothing().when(rideUtilsMock).handleNotificationsAndProcessReservations(any(), anyList());

        RideSimpleDisplayDTO rideSimpleDisplayDTOMock = mock(RideSimpleDisplayDTO.class);
        when(rideUtilsMock.createBasicRideSimpleDisplayDTO(any(), any())).thenReturn(rideSimpleDisplayDTOMock);

        assertEquals(rideService.orderBasicRide(dto, authenticationMock), rideSimpleDisplayDTOMock);
        verify(passengerMock).setTokenBalance(anyInt());
        verify(passengerRepositoryMock).save(passengerMock);
        verify(rideUtilsMock).handleNotificationsAndProcessReservations(any(), anyList());
    }


    // split fare ride
    @Test
    void Throw_exception_when_vehicle_type_does_not_exist_when_ordering_a_split_fare_ride() {
        SplitFareRideCreationDTO dto = mock(SplitFareRideCreationDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);
        when(dto.getVehicleType()).thenReturn(null);
        assertThrows(
                NoSuchElementException.class,
                () -> rideService.orderSplitFareRide(dto, authentication),
                "Expected orderSplitFareRide() to throw NoSuchElementException, but it didn't"
        );
    }

    @Test
    void Throw_exception_when_a_linked_passenger_does_not_exist_when_ordering_a_split_fare_ride() {
        SplitFareRideCreationDTO dto = mock(SplitFareRideCreationDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);
        when(dto.getVehicleType()).thenReturn("COUPE");
        when(vehicleTypeRepositoryMock.findByName("COUPE")).thenReturn(Optional.of(vehicleTypeMock));

        when(rideUtilsMock.calculateRidePrice(dto, vehicleTypeMock)).thenReturn(1300);
        when(dto.getUsersToPay()).thenReturn(new ArrayList<>(3));

        doThrow(UserDoesNotExistException.class)
                .when(rideUtilsMock).checkIfSplitFareRideIsValid(any(), any(), anyInt());

        assertThrows(
                UserDoesNotExistException.class,
                () -> rideService.orderSplitFareRide(dto, authentication),
                "Expected orderSplitFareRide() to throw UserDoesNotExistException, but it didn't"
        );
    }

    @Test
    void Throw_exception_when_there_are_duplicate_passengers_when_ordering_a_split_fare_ride() {
        SplitFareRideCreationDTO dto = mock(SplitFareRideCreationDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);
        when(dto.getVehicleType()).thenReturn("COUPE");
        when(vehicleTypeRepositoryMock.findByName("COUPE")).thenReturn(Optional.of(vehicleTypeMock));

        when(rideUtilsMock.calculateRidePrice(dto, vehicleTypeMock)).thenReturn(1300);
        when(dto.getUsersToPay()).thenReturn(new ArrayList<>(3));

        doThrow(LinkedPassengersNotAllDistinctException.class)
                .when(rideUtilsMock).checkIfSplitFareRideIsValid(any(), any(), anyInt());

        assertThrows(
                LinkedPassengersNotAllDistinctException.class,
                () -> rideService.orderSplitFareRide(dto, authentication),
                "Expected orderSplitFareRide() to throw LinkedPassengersNotAllDistinctException, but it didn't"
        );
    }

    @Test
    void Throw_exception_when_a_passenger_already_has_an_active_ride_when_ordering_a_split_fare_ride() {
        SplitFareRideCreationDTO dto = mock(SplitFareRideCreationDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);
        when(dto.getVehicleType()).thenReturn("COUPE");
        when(vehicleTypeRepositoryMock.findByName("COUPE")).thenReturn(Optional.of(vehicleTypeMock));

        when(rideUtilsMock.calculateRidePrice(dto, vehicleTypeMock)).thenReturn(1300);
        when(dto.getUsersToPay()).thenReturn(new ArrayList<>(3));

        doThrow(PassengerAlreadyHasAnActiveRideException.class)
                .when(rideUtilsMock).checkIfSplitFareRideIsValid(any(), any(), anyInt());

        assertThrows(
                PassengerAlreadyHasAnActiveRideException.class,
                () -> rideService.orderSplitFareRide(dto, authentication),
                "Expected orderSplitFareRide() to throw PassengerAlreadyHasAnActiveRideException, but it didn't"
        );
    }

    @Test
    void Throw_exception_when_the_order_sender_passenger_lacks_tokens_when_ordering_a_split_fare_ride() {
        SplitFareRideCreationDTO dto = mock(SplitFareRideCreationDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);
        when(dto.getVehicleType()).thenReturn("COUPE");
        when(vehicleTypeRepositoryMock.findByName("COUPE")).thenReturn(Optional.of(vehicleTypeMock));

        when(rideUtilsMock.calculateRidePrice(dto, vehicleTypeMock)).thenReturn(1300);
        when(dto.getUsersToPay()).thenReturn(new ArrayList<>(3));

        doThrow(InsufficientFundsException.class)
                .when(rideUtilsMock).checkIfSplitFareRideIsValid(any(), any(), anyInt());

        assertThrows(
                InsufficientFundsException.class,
                () -> rideService.orderSplitFareRide(dto, authentication),
                "Expected orderSplitFareRide() to throw InsufficientFundsException, but it didn't"
        );
    }

    @Test
    void Throw_exception_when_delay_is_shorter_than_20_for_a_scheduled_split_fare_ride() {
        SplitFareRideCreationDTO dto = mock(SplitFareRideCreationDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);
        when(dto.getVehicleType()).thenReturn("COUPE");
        when(vehicleTypeRepositoryMock.findByName("COUPE")).thenReturn(Optional.of(vehicleTypeMock));

        when(rideUtilsMock.calculateRidePrice(dto, vehicleTypeMock)).thenReturn(1300);
        when(dto.getUsersToPay()).thenReturn(new ArrayList<>(3));

        doThrow(ReservationTooSoonException.class)
                .when(rideUtilsMock).checkIfSplitFareRideIsValid(any(), any(), anyInt());

        assertThrows(
                ReservationTooSoonException.class,
                () -> rideService.orderSplitFareRide(dto, authentication),
                "Expected orderSplitFareRide() to throw ReservationTooSoonException, but it didn't"
        );
    }


    @Test
    void Return_true_after_a_successful_creation_of_an_immediate_split_fare_ride() {
        SplitFareRideCreationDTO dto = mock(SplitFareRideCreationDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);
        when(dto.getVehicleType()).thenReturn("COUPE");
        when(vehicleTypeRepositoryMock.findByName("COUPE")).thenReturn(Optional.of(vehicleTypeMock));

        when(rideUtilsMock.calculateRidePrice(dto, vehicleTypeMock)).thenReturn(1300);
        List<String> usersToPay = new ArrayList<>(3);
        usersToPay.add("username1@noemail.com");
        usersToPay.add("username2@noemail.com");
        usersToPay.add("username3@noemail.com");

        when(dto.getUsersToPay()).thenReturn(usersToPay);
        doNothing().when(rideUtilsMock).checkIfSplitFareRideIsValid(any(), any(), anyInt());

        when(rideUtilsMock.createSplitFareRide(any(), anyInt())).thenReturn(rideMock);
        doNothing().when(passengerMock).setTokenBalance(anyInt());
        when(passengerRepositoryMock.save(passengerMock)).thenReturn(passengerMock);
        doNothing().when(rideUtilsMock).createPassengerRideForUsers(any(), any(), anyInt(), any());

        when(passengerMock.getUsername()).thenReturn("username1@noemail.com");
        doNothing().when(rideUtilsMock).sendRefreshMessage(anyString());

        RideService rideServiceSpy = Mockito.spy(rideService);
        doNothing().when(rideServiceSpy).scheduleExecution(any(), anyLong(), any());

        assertTrue(rideServiceSpy.orderSplitFareRide(dto, authentication));
        verify(passengerMock).setTokenBalance(anyInt());
        verify(passengerRepositoryMock).save(passengerMock);
        verify(rideUtilsMock).createPassengerRideForUsers(any(), any(), anyInt(), any());
        verify(rideServiceSpy).scheduleExecution(any(), anyLong(), any());
    }


    // passenger reject ride
    @Test
    void Throw_exception_when_attempting_to_reject_a_ride_that_does_not_exist() {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> rideService.rejectRide(dto, authentication),
                "Expected rejectRide() to throw NoSuchElementException, but it didn't"
        );
    }

    @Test
    void Throw_exception_when_attempting_to_reject_a_passenger_ride_that_does_not_exist() {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));

        List<PassengerRide> passengerRidesMock = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            passengerRidesMock.add(Mockito.mock(PassengerRide.class));
        }
        when(passengerRideRepositoryMock.findByRide(rideMock)).thenReturn(passengerRidesMock);

        when(passengerMock.getUsername()).thenReturn("username1@noemail.com");
        when(passengerRideRepositoryMock.findByRideAndPassengerUsername(rideMock, "username1@noemail.com")).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> rideService.rejectRide(dto, authentication),
                "Expected rejectRide() to throw NoSuchElementException, but it didn't"
        );
    }

    @Test
    void Return_false_when_attempting_to_reject_ride_after_all_passengers_have_confirmed() {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));

        List<PassengerRide> passengerRidesMock = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            passengerRidesMock.add(Mockito.mock(PassengerRide.class));
        }
        when(passengerRideRepositoryMock.findByRide(rideMock)).thenReturn(passengerRidesMock);

        PassengerRide currentPassengerRideMock = Mockito.mock(PassengerRide.class);
        when(passengerMock.getUsername()).thenReturn("username1@noemail.com");
        when(passengerRideRepositoryMock.findByRideAndPassengerUsername(rideMock, "username1@noemail.com"))
                .thenReturn(Optional.of(currentPassengerRideMock));

        when(rideMock.getPassengersConfirmed()).thenReturn(true);

        assertFalse(rideService.rejectRide(dto, authentication));
    }

    @Test
    void Return_false_when_attempting_to_reject_ride_after_the_passenger_has_already_confirmed() {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));

        List<PassengerRide> passengerRidesMock = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            passengerRidesMock.add(Mockito.mock(PassengerRide.class));
        }
        when(passengerRideRepositoryMock.findByRide(rideMock)).thenReturn(passengerRidesMock);

        PassengerRide currentPassengerRideMock = Mockito.mock(PassengerRide.class);
        when(passengerMock.getUsername()).thenReturn("username1@noemail.com");
        when(passengerRideRepositoryMock.findByRideAndPassengerUsername(rideMock, "username1@noemail.com"))
                .thenReturn(Optional.of(currentPassengerRideMock));

        when(rideMock.getPassengersConfirmed()).thenReturn(false);
        when(currentPassengerRideMock.isAgreed()).thenReturn(true);

        assertFalse(rideService.rejectRide(dto, authentication));
    }

    @Test
    void Return_true_when_rejection_is_valid() {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));

        List<PassengerRide> passengerRidesMock = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            passengerRidesMock.add(Mockito.mock(PassengerRide.class));
        }
        when(passengerRideRepositoryMock.findByRide(rideMock)).thenReturn(passengerRidesMock);

        PassengerRide currentPassengerRideMock = Mockito.mock(PassengerRide.class);
        when(passengerMock.getUsername()).thenReturn("username1@noemail.com");
        when(passengerRideRepositoryMock.findByRideAndPassengerUsername(rideMock, "username1@noemail.com"))
                .thenReturn(Optional.of(currentPassengerRideMock));

        when(rideMock.getPassengersConfirmed()).thenReturn(false);
        when(currentPassengerRideMock.isAgreed()).thenReturn(false);

        List<Passenger> passengerRidesPassengersMock = new ArrayList<>();
        passengerRidesPassengersMock.add(passengerMock);
        for (int i = 2; i < 5; i++) {
            Passenger p = Mockito.mock(Passenger.class);
            when(p.getUsername()).thenReturn("username" + i + "@noemail.com");
            passengerRidesPassengersMock.add(p);
        }
        for (int i = 0; i < 4; i++) {
            when(passengerRidesMock.get(i).getPassenger()).thenReturn(passengerRidesPassengersMock.get(i));
        }

        doNothing().when(rideUtilsMock).sendMessageToPassenger(anyString(), anyString(), any());
        doNothing().when(rideUtilsMock).refundPassengers(any());
        doNothing().when(rideMock).setStatus(RideStatus.CANCELLED);
        when(rideRepositoryMock.save(rideMock)).thenReturn(rideMock);

        assertTrue(rideService.rejectRide(dto, authentication));
        verify(rideUtilsMock, times(3)).sendMessageToPassenger(anyString(), anyString(), any());
        verify(rideUtilsMock).refundPassengers(any());
        verify(rideMock).setStatus(RideStatus.CANCELLED);
        verify(rideRepositoryMock).save(rideMock);
    }


    // ride confirmation
    @Test
    void Throw_exception_when_attempting_to_confirm_a_ride_that_does_not_exist() {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> rideService.confirmRide(dto, authentication),
                "Expected confirmRide() to throw NoSuchElementException, but it didn't"
        );
    }

    @Test
    void Throw_exception_when_attempting_to_confirm_a_passenger_ride_that_does_not_exist() {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));

        List<PassengerRide> passengerRidesMock = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            passengerRidesMock.add(Mockito.mock(PassengerRide.class));
        }
        when(passengerRideRepositoryMock.findByRide(rideMock)).thenReturn(passengerRidesMock);

        List<Passenger> passengerRidesPassengersMock = new ArrayList<>();
        passengerRidesPassengersMock.add(passengerMock);
        for (int i = 0; i < 3; i++) {
            Passenger p = Mockito.mock(Passenger.class);
            when(p.getUsername()).thenReturn("username" + (i + 2) + "@noemail.com");
            passengerRidesPassengersMock.add(p);
        }
        for (int i = 0; i < 4; i++) {
            when(passengerRidesMock.get(i).getPassenger()).thenReturn(passengerRidesPassengersMock.get(i));
        }

        when(passengerMock.getUsername()).thenReturn("username1@noemail.com");
        when(passengerRideRepositoryMock.findByRideAndPassengerUsername(rideMock, "username1@noemail.com")).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> rideService.confirmRide(dto, authentication),
                "Expected confirmRide() to throw NoSuchElementException, but it didn't"
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 100, 999})
    void Throw_exception_when_attempting_to_confirm_a_ride_with_insufficient_token_balance(int passengerTokenBalance) {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));

        List<PassengerRide> passengerRidesMock = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            passengerRidesMock.add(Mockito.mock(PassengerRide.class));
        }
        when(passengerRideRepositoryMock.findByRide(rideMock)).thenReturn(passengerRidesMock);

        List<Passenger> passengerRidesPassengersMock = new ArrayList<>();
        passengerRidesPassengersMock.add(passengerMock);
        for (int i = 0; i < 3; i++) {
            Passenger p = Mockito.mock(Passenger.class);
            when(p.getUsername()).thenReturn("username" + (i + 2) + "@noemail.com");
            passengerRidesPassengersMock.add(p);
        }
        for (int i = 0; i < 4; i++) {
            when(passengerRidesMock.get(i).getPassenger()).thenReturn(passengerRidesPassengersMock.get(i));
        }

        when(passengerMock.getUsername()).thenReturn("username1@noemail.com");
        when(passengerRideRepositoryMock
                .findByRideAndPassengerUsername(any(), anyString())).thenReturn(Optional.of(passengerRideMock));

        when(passengerMock.getTokenBalance()).thenReturn(passengerTokenBalance);
        when(passengerRideMock.getFare()).thenReturn(1000);

        doNothing().when(rideUtilsMock).sendMessageToPassenger(anyString(), anyString(), any());
        doNothing().when(rideMock).setStatus(RideStatus.CANCELLED);
        when(rideRepositoryMock.save(rideMock)).thenReturn(rideMock);
        doNothing().when(rideUtilsMock).refundPassengers(anyList());

        assertThrows(
                InsufficientFundsException.class,
                () -> rideService.confirmRide(dto, authentication),
                "Expected confirmRide() to throw InsufficientFundsException, but it didn't"
        );
        verify(rideUtilsMock, times(4)).sendMessageToPassenger(anyString(), anyString(), any());
        verify(rideMock).setStatus(RideStatus.CANCELLED);
        verify(rideRepositoryMock).save(rideMock);
        verify(rideUtilsMock).refundPassengers(anyList());
    }

    @ParameterizedTest
    @ValueSource(ints = {1000, 1500, 5000})
    void Return_true_when_passenger_is_not_the_last_one_to_confirm_the_ride(int passengerTokenBalance) {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);
        when(passengerMock.getUsername()).thenReturn("username1@noemail.com");

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));

        List<PassengerRide> passengerRidesMock = new ArrayList<>();
        passengerRidesMock.add(passengerRideMock);
        for (int i = 0; i < 3; i++) {
            PassengerRide pr = Mockito.mock(PassengerRide.class);
            passengerRidesMock.add(pr);
        }
        when(passengerRideRepositoryMock.findByRide(rideMock)).thenReturn(passengerRidesMock);

        List<Passenger> passengerRidesPassengersMock = new ArrayList<>();
        passengerRidesPassengersMock.add(passengerMock);
        for (int i = 0; i < 3; i++) {
            Passenger p = Mockito.mock(Passenger.class);
            when(p.getUsername()).thenReturn("username" + (i + 2) + "@noemail.com");
            passengerRidesPassengersMock.add(p);
        }
        for (int i = 0; i < 4; i++) {
            when(passengerRidesMock.get(i).getPassenger()).thenReturn(passengerRidesPassengersMock.get(i));
            if (i < 3) {
                when(passengerRidesMock.get(i).isAgreed()).thenReturn(true);
            }
            else {
                when(passengerRidesMock.get(i).isAgreed()).thenReturn(false);
            }
            when(passengerRideRepositoryMock.findByRideAndPassengerUsername(rideMock,
                    passengerRidesMock.get(i).getPassenger().getUsername()))
                    .thenReturn(Optional.of(passengerRidesMock.get(i)));
        }

        when(passengerMock.getTokenBalance()).thenReturn(passengerTokenBalance);
        when(passengerRideMock.getFare()).thenReturn(1000);

        doNothing().when(passengerMock).setTokenBalance(anyInt());
        when(passengerRideRepositoryMock.save(passengerRideMock)).thenReturn(passengerRideMock);
        when(passengerRepositoryMock.save(passengerMock)).thenReturn(passengerMock);

        assertTrue(rideService.confirmRide(dto, authentication));
        verify(passengerRideRepositoryMock).save(passengerRideMock);
        verify(passengerRepositoryMock).save(passengerMock);
    }

    @ParameterizedTest
    @ValueSource(ints = {1000, 1500, 5000})
    void Return_true_when_passenger_is_the_last_one_to_confirm_an_immediate_ride(int passengerTokenBalance) {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);
        when(passengerMock.getUsername()).thenReturn("username1@noemail.com");

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));

        List<PassengerRide> passengerRidesMock = new ArrayList<>();
        passengerRidesMock.add(passengerRideMock);
        for (int i = 0; i < 3; i++) {
            PassengerRide pr = Mockito.mock(PassengerRide.class);
            passengerRidesMock.add(pr);
        }
        when(passengerRideRepositoryMock.findByRide(rideMock)).thenReturn(passengerRidesMock);

        List<Passenger> passengerRidesPassengersMock = new ArrayList<>();
        passengerRidesPassengersMock.add(passengerMock);
        for (int i = 0; i < 3; i++) {
            Passenger p = Mockito.mock(Passenger.class);
            when(p.getUsername()).thenReturn("username" + (i + 2) + "@noemail.com");
            passengerRidesPassengersMock.add(p);
        }
        for (int i = 0; i < 4; i++) {
            when(passengerRidesMock.get(i).getPassenger()).thenReturn(passengerRidesPassengersMock.get(i));
            when(passengerRidesMock.get(i).isAgreed()).thenReturn(true);
            when(passengerRideRepositoryMock.findByRideAndPassengerUsername(rideMock,
                    passengerRidesMock.get(i).getPassenger().getUsername()))
                    .thenReturn(Optional.of(passengerRidesMock.get(i)));
        }

        when(passengerMock.getTokenBalance()).thenReturn(passengerTokenBalance);
        when(passengerRideMock.getFare()).thenReturn(1000);

        doNothing().when(passengerMock).setTokenBalance(anyInt());
        when(passengerRideRepositoryMock.save(passengerRideMock)).thenReturn(passengerRideMock);
        when(passengerRepositoryMock.save(passengerMock)).thenReturn(passengerMock);

        doNothing().when(rideMock).setPassengersConfirmed(true);
        doNothing().when(rideMock).setStatus(RideStatus.RESERVED);
        when(rideRepositoryMock.save(rideMock)).thenReturn(null);

        when(rideMock.getDelayInMinutes()).thenReturn(0);
        doNothing().when(rideUtilsMock).processReservation(rideMock, passengerRidesMock);

        assertTrue(rideService.confirmRide(dto, authentication));
        verify(passengerRideRepositoryMock).save(passengerRideMock);
        verify(passengerRepositoryMock).save(passengerMock);
        verify(rideMock).setPassengersConfirmed(true);
        verify(rideMock).setStatus(RideStatus.RESERVED);
        verify(rideRepositoryMock).save(rideMock);
        verify(rideUtilsMock).processReservation(rideMock, passengerRidesMock);
    }

    @ParameterizedTest
    @ValueSource(ints = {1000, 1500, 5000})
    void Return_true_when_passenger_is_the_last_one_to_confirm_a_scheduled_ride(int passengerTokenBalance) {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);
        when(passengerMock.getUsername()).thenReturn("username1@noemail.com");

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));

        List<PassengerRide> passengerRidesMock = new ArrayList<>();
        passengerRidesMock.add(passengerRideMock);
        for (int i = 0; i < 3; i++) {
            PassengerRide pr = Mockito.mock(PassengerRide.class);
            passengerRidesMock.add(pr);
        }
        when(passengerRideRepositoryMock.findByRide(any())).thenReturn(passengerRidesMock);

        List<Passenger> passengerRidesPassengersMock = new ArrayList<>();
        passengerRidesPassengersMock.add(passengerMock);
        for (int i = 0; i < 3; i++) {
            Passenger p = Mockito.mock(Passenger.class);
            when(p.getUsername()).thenReturn("username" + (i + 2) + "@noemail.com");
            passengerRidesPassengersMock.add(p);
        }
        for (int i = 0; i < 4; i++) {
            when(passengerRidesMock.get(i).getPassenger()).thenReturn(passengerRidesPassengersMock.get(i));
            when(passengerRidesMock.get(i).isAgreed()).thenReturn(true);
            when(passengerRideRepositoryMock.findByRideAndPassengerUsername(rideMock,
                    passengerRidesMock.get(i).getPassenger().getUsername()))
                    .thenReturn(Optional.of(passengerRidesMock.get(i)));
        }

        when(passengerMock.getTokenBalance()).thenReturn(passengerTokenBalance);
        when(passengerRideMock.getFare()).thenReturn(1000);

        doNothing().when(passengerMock).setTokenBalance(anyInt());
        when(passengerRideRepositoryMock.save(passengerRideMock)).thenReturn(passengerRideMock);
        when(passengerRepositoryMock.save(passengerMock)).thenReturn(passengerMock);

        doNothing().when(rideMock).setPassengersConfirmed(true);
        doNothing().when(rideMock).setStatus(RideStatus.RESERVED);
        when(rideRepositoryMock.save(rideMock)).thenReturn(null);

        when(rideMock.getDelayInMinutes()).thenReturn(20);
        doNothing().when(rideUtilsMock).sendRefreshMessageToMultipleUsers(anyList());
        doNothing().when(rideUtilsMock).handleNotificationsAndProcessReservations(rideMock, passengerRidesMock);

        assertTrue(rideService.confirmRide(dto, authentication));
        verify(passengerRideRepositoryMock).save(passengerRideMock);
        verify(passengerRepositoryMock).save(passengerMock);
        verify(rideMock).setPassengersConfirmed(true);
        verify(rideMock).setStatus(RideStatus.RESERVED);
        verify(rideRepositoryMock).save(rideMock);
        verify(rideUtilsMock).sendRefreshMessageToMultipleUsers(anyList());
        verify(rideUtilsMock).handleNotificationsAndProcessReservations(rideMock, passengerRidesMock);
    }


    // report inconsistency
    @Test
    void Throw_exception_when_reporting_inconsistency_on_a_non_existent_ride() {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> rideService.reportInconsistency(dto, authentication),
                "Expected reportInconsistency() to throw NoSuchElementException, but it didn't"
        );
    }

    @Test
    void Throw_exception_when_reporting_inconsistency_on_a_ride_that_does_not_belong_to_passenger() {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);
        when(passengerMock.getUsername()).thenReturn("username1@noemail.com");

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));

        when(passengerRideRepositoryMock.findByRideAndPassengerUsername(rideMock, "username1@noemail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                RideDoesNotBelongToPassengerException.class,
                () -> rideService.reportInconsistency(dto, authentication),
                "Expected reportInconsistency() to throw RideDoesNotBelongToPassengerException, but it didn't"
        );
    }

    @Test
    void Return_true_when_inconsistency_report_is_valid() {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(passengerMock);
        when(passengerMock.getUsername()).thenReturn("username1@noemail.com");

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));

        when(passengerRideRepositoryMock.findByRideAndPassengerUsername(rideMock, "username1@noemail.com"))
                .thenReturn(Optional.of(passengerRideMock));

        doNothing().when(rideMock).setDriverInconsistencyReported(true);
        when(rideRepositoryMock.save(rideMock)).thenReturn(rideMock);
        doNothing().when(rideUtilsMock).sendRefreshMessageToDriverAndAllPassengers(rideMock);

        assertTrue(rideService.reportInconsistency(dto, authentication));
        verify(rideMock).setDriverInconsistencyReported(true);
        verify(rideRepositoryMock).save(rideMock);
        verify(rideUtilsMock).sendRefreshMessageToDriverAndAllPassengers(rideMock);
    }


    // begin ride
    @Test
    void Throw_exception_when_attempting_to_begin_a_non_existent_ride() {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(driverMock);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> rideService.beginRide(dto, authentication),
                "Expected beginRide() to throw NoSuchElementException, but it didn't"
        );
    }

    @Test
    void Return_true_after_successfully_starting_the_ride() {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(driverMock);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));

        doNothing().when(rideMock).setStatus(RideStatus.IN_PROGRESS);
        doNothing().when(rideMock).setStartTime(any());
        when(rideRepositoryMock.save(rideMock)).thenReturn(rideMock);

        Route routeMock = mock(Route.class);
        when(rideMock.getRoute()).thenReturn(routeMock);
        List<Coordinates> waypointsMock = new ArrayList<>(2);
        for (int i = 0; i < 2; i++) {
            waypointsMock.add(Mockito.mock(Coordinates.class));
        }
        when(routeMock.getWaypoints()).thenReturn(waypointsMock);

        when(rideMock.getDriver()).thenReturn(driverMock);
        Vehicle vehicleMock = mock(Vehicle.class);
        when(driverMock.getVehicle()).thenReturn(vehicleMock);
        when(vehicleMock.getExpectedTripTime()).thenReturn(100L);

        doNothing().when(rideUtilsMock).directDriverToLocation(any(), any());
        doNothing().when(rideUtilsMock).sendRefreshMessageToDriverAndAllPassengers(rideMock);
        RideService rideServiceSpy = Mockito.spy(rideService);
        doNothing().when(rideServiceSpy).scheduleExecution(any(), anyLong(), any());

        assertTrue(rideServiceSpy.beginRide(dto, authentication));
        verify(rideMock).setStatus(RideStatus.IN_PROGRESS);
        verify(rideMock).setStartTime(any());
        verify(rideRepositoryMock).save(rideMock);
        verify(rideUtilsMock).directDriverToLocation(any(), any());
        verify(rideUtilsMock).sendRefreshMessageToDriverAndAllPassengers(rideMock);
        verify(rideServiceSpy).scheduleExecution(any(), anyLong(), any());
    }


    // complete ride
    @Test
    void Throw_exception_when_attempting_to_complete_a_non_existent_ride() {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(driverMock);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> rideService.completeRide(dto, authentication),
                "Expected completeRide() to throw NoSuchElementException, but it didn't"
        );
    }

    @Test
    void Return_true_after_successfully_completing_a_ride() {
        RideIdDTO dto = mock(RideIdDTO.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(driverMock);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));
        doNothing().when(rideMock).setStatus(RideStatus.COMPLETED);
        doNothing().when(rideMock).setEndTime(any());
        when(rideRepositoryMock.save(rideMock)).thenReturn(rideMock);

        doNothing().when(driverMock).setRidesCompleted(anyInt());
        doNothing().when(driverMock).setDistanceTravelled(anyDouble());
        when(driverRepositoryMock.save(driverMock)).thenReturn(driverMock);

        List<PassengerRide> passengerRidesMock = new ArrayList<>();
        passengerRidesMock.add(passengerRideMock);
        for (int i = 0; i < 3; i++) {
            passengerRidesMock.add(Mockito.mock(PassengerRide.class));
        }
        when(passengerRideRepositoryMock.findByRide(rideMock)).thenReturn(passengerRidesMock);

        List<Passenger> passengerRidesPassengersMock = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Passenger p = Mockito.mock(Passenger.class);
            when(p.getUsername()).thenReturn("username" + (i + 1) + "@noemail.com");
            doReturn(i).when(p).getRidesCompleted();
            doNothing().when(p).setRidesCompleted(anyInt());
            doReturn(i + 1.0).when(p).getDistanceTravelled();
            doNothing().when(p).setDistanceTravelled(anyDouble());
            passengerRidesPassengersMock.add(p);
        }
        when(passengerRepositoryMock.save(any())).thenReturn(null);
        for (int i = 0; i < 4; i++) {
            when(passengerRidesMock.get(i).getPassenger()).thenReturn(passengerRidesPassengersMock.get(i));
        }
        doNothing().when(rideUtilsMock).sendMessageToMultiplePassengers(any(), anyString(), any());

        doNothing().when(rideUtilsMock).updateCurrentDriverRide(driverMock);
        when(driverMock.getUsername()).thenReturn("drivertest@noemail.com");
        doNothing().when(rideUtilsMock).sendRefreshMessage(anyString());

        assertTrue(rideService.completeRide(dto, authentication));
        verify(rideRepositoryMock).findById(anyInt());
        verify(rideMock).setStatus(RideStatus.COMPLETED);
        verify(rideMock).setEndTime(any());
        verify(rideRepositoryMock).save(rideMock);
        verify(driverMock).setRidesCompleted(anyInt());
        verify(driverMock).setDistanceTravelled(anyDouble());
        verify(driverRepositoryMock).save(driverMock);
        verify(rideUtilsMock).updateCurrentDriverRide(driverMock);
        verify(rideUtilsMock).sendRefreshMessage(anyString());
    }


    // driver reject ride
    @Test
    void Throw_exception_when_a_driver_is_attempting_to_reject_a_non_existent_ride() {
        DriverRideRejectionCreationDTO dto = mock(DriverRideRejectionCreationDTO.class);
        Authentication authentication = mock(Authentication.class);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> rideService.driverRejectRide(dto, authentication),
                "Expected driverRejectRide() to throw NoSuchElementException, but it didn't"
        );
    }

    @Test
    void Return_true_when_driver_successfully_submits_a_ride_rejection_request() {
        DriverRideRejectionCreationDTO dto = mock(DriverRideRejectionCreationDTO.class);
        Authentication authentication = mock(Authentication.class);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));

        when(dto.getReason()).thenReturn("reason");
        doNothing().when(rideMock).setDriverRejectionReason("reason");
        when(rideRepositoryMock.save(rideMock)).thenReturn(rideMock);

        assertTrue(rideService.driverRejectRide(dto, authentication));
        verify(rideMock).setDriverRejectionReason("reason");
        verify(rideRepositoryMock).save(rideMock);
    }


    // accept driver ride rejection
    @Test
    void Throw_exception_when_attempting_to_accept_driver_rejection_for_non_existent_ride() {
        DriverRideRejectionVerdictCreationDTO dto = mock(DriverRideRejectionVerdictCreationDTO.class);
        Authentication authentication = mock(Authentication.class);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> rideService.acceptDriverRideRejection(dto, authentication),
                "Expected acceptDriverRideRejection() to throw NoSuchElementException, but it didn't"
        );
    }

    @Test
    void Return_true_when_denying_driver_ride_rejection() {
        DriverRideRejectionVerdictCreationDTO dto = mock(DriverRideRejectionVerdictCreationDTO.class);
        Authentication authentication = mock(Authentication.class);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));

        when(dto.isAccepted()).thenReturn(false);

        doNothing().when(rideMock).setDriverRejectionReason(null);
        when(rideRepositoryMock.save(rideMock)).thenReturn(rideMock);
        when(driverMock.getUsername()).thenReturn("drivertest@noemail.com");
        when(rideMock.getDriver()).thenReturn(driverMock);
        doNothing().when(rideUtilsMock).sendMessageToDriver(anyString(), anyString(), any());

        assertTrue(rideService.acceptDriverRideRejection(dto, authentication));
        verify(rideMock).setDriverRejectionReason(null);
        verify(rideRepositoryMock).save(rideMock);
        verify(rideUtilsMock).sendMessageToDriver(anyString(), anyString(), any());
    }

    @Test
    void Return_true_when_accepting_driver_ride_rejection_and_driver_has_no_next_ride() {
        DriverRideRejectionVerdictCreationDTO dto = mock(DriverRideRejectionVerdictCreationDTO.class);
        Authentication authentication = mock(Authentication.class);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));

        when(dto.isAccepted()).thenReturn(true);

        doNothing().when(rideMock).setStatus(RideStatus.CANCELLED);
        when(rideRepositoryMock.save(rideMock)).thenReturn(rideMock);

        when(rideMock.getDriver()).thenReturn(driverMock);
        when(driverMock.getNextRide()).thenReturn(null);

        doNothing().when(driverMock).setActive(false);
        doNothing().when(driverMock).setCurrentRide(null);
        doNothing().when(driverMock).setNextRide(null);

        when(driverMock.getVehicle()).thenReturn(vehicleMock);
        when(vehicleMock.getNextCoordinates()).thenReturn(mock(Coordinates.class));
        doNothing().when(vehicleMock).setCurrentCoordinates(any());
        doNothing().when(vehicleMock).setExpectedTripTime(0);
        doNothing().when(vehicleMock).setRideActive(false);
        doNothing().when(vehicleMock).setCoordinatesChangedAt(any());
        when(vehicleRepositoryMock.save(vehicleMock)).thenReturn(vehicleMock);

        List<PassengerRide> passengerRidesMock = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            passengerRidesMock.add(Mockito.mock(PassengerRide.class));
        }
        when(passengerRideRepositoryMock.findByRide(rideMock)).thenReturn(passengerRidesMock);

        doNothing().when(rideUtilsMock).handleRejectedRidePassengers(passengerRidesMock);
        when(rideMock.getDriver()).thenReturn(driverMock);
        when(driverMock.getUsername()).thenReturn("drivertest@noemail.com");
        doNothing().when(rideUtilsMock).sendMessageToDriver(anyString(), anyString(), any());

        assertTrue(rideService.acceptDriverRideRejection(dto, authentication));
        verify(rideMock).setStatus(RideStatus.CANCELLED);
        verify(driverMock).setActive(false);
        verify(driverMock).setCurrentRide(null);
        verify(driverMock).setNextRide(null);
        verify(vehicleMock).setCurrentCoordinates(any());
        verify(vehicleMock).setExpectedTripTime(0);
        verify(vehicleMock).setRideActive(false);
        verify(vehicleMock).setCoordinatesChangedAt(any());
        verify(vehicleRepositoryMock).save(vehicleMock);
        verify(rideUtilsMock).handleRejectedRidePassengers(passengerRidesMock);
        verify(rideUtilsMock).sendMessageToDriver(anyString(), anyString(), any());
    }

    @Test
    void Return_true_when_accepting_driver_ride_rejection_and_driver_has_a_next_ride() {
        DriverRideRejectionVerdictCreationDTO dto = mock(DriverRideRejectionVerdictCreationDTO.class);
        Authentication authentication = mock(Authentication.class);

        when(dto.getRideId()).thenReturn(0);
        when(rideRepositoryMock.findById(0)).thenReturn(Optional.of(rideMock));

        when(dto.isAccepted()).thenReturn(true);

        doNothing().when(rideMock).setStatus(RideStatus.CANCELLED);
        when(rideRepositoryMock.save(rideMock)).thenReturn(rideMock);

        when(rideMock.getDriver()).thenReturn(driverMock);

        Ride nextRideMock = mock(Ride.class);
        when(nextRideMock.getId()).thenReturn(1);
        when(driverMock.getNextRide()).thenReturn(nextRideMock);
        when(rideRepositoryMock.findById(1)).thenReturn(Optional.of(nextRideMock));
        doNothing().when(nextRideMock).setStatus(RideStatus.CANCELLED);
        when(rideRepositoryMock.save(nextRideMock)).thenReturn(nextRideMock);
        List<PassengerRide> nextPassengerRidesMock = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            nextPassengerRidesMock.add(Mockito.mock(PassengerRide.class));
        }
        when(passengerRideRepositoryMock.findByRide(nextRideMock)).thenReturn(nextPassengerRidesMock);
        doNothing().when(rideUtilsMock).handleRejectedRidePassengers(nextPassengerRidesMock);

        doNothing().when(driverMock).setActive(false);
        doNothing().when(driverMock).setCurrentRide(null);
        doNothing().when(driverMock).setNextRide(null);

        when(driverMock.getVehicle()).thenReturn(vehicleMock);
        when(vehicleMock.getNextCoordinates()).thenReturn(mock(Coordinates.class));
        doNothing().when(vehicleMock).setCurrentCoordinates(any());
        doNothing().when(vehicleMock).setExpectedTripTime(0);
        doNothing().when(vehicleMock).setRideActive(false);
        doNothing().when(vehicleMock).setCoordinatesChangedAt(any());
        when(vehicleRepositoryMock.save(vehicleMock)).thenReturn(vehicleMock);

        List<PassengerRide> passengerRidesMock = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            passengerRidesMock.add(Mockito.mock(PassengerRide.class));
        }
        when(passengerRideRepositoryMock.findByRide(rideMock)).thenReturn(passengerRidesMock);

        doNothing().when(rideUtilsMock).handleRejectedRidePassengers(passengerRidesMock);
        when(rideMock.getDriver()).thenReturn(driverMock);
        when(driverMock.getUsername()).thenReturn("drivertest@noemail.com");
        doNothing().when(rideUtilsMock).sendMessageToDriver(anyString(), anyString(), any());

        assertTrue(rideService.acceptDriverRideRejection(dto, authentication));
        verify(rideMock).setStatus(RideStatus.CANCELLED);
        verify(nextRideMock).setStatus(RideStatus.CANCELLED);
        verify(rideRepositoryMock).save(nextRideMock);
        verify(driverMock).setActive(false);
        verify(driverMock).setCurrentRide(null);
        verify(driverMock).setNextRide(null);
        verify(vehicleMock).setCurrentCoordinates(any());
        verify(vehicleMock).setExpectedTripTime(0);
        verify(vehicleMock).setRideActive(false);
        verify(vehicleMock).setCoordinatesChangedAt(any());
        verify(vehicleRepositoryMock).save(vehicleMock);
        verify(rideUtilsMock).handleRejectedRidePassengers(passengerRidesMock);
        verify(rideUtilsMock).sendMessageToDriver(anyString(), anyString(), any());
    }
}
