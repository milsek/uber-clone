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
    
}
