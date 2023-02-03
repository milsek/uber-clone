package com.example.springbackend.controller;

import com.example.springbackend.dto.creation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import javax.transaction.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(Transactional.TxType.REQUIRED)
public class RideControllerIntegrationTests {
    private static final String URL_PREFIX = "/api/rides";
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeAll
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    // basic ride
    @Test
    @DisplayName("Should return 200 and the created basic ride [POST] " + URL_PREFIX + "/basic")
    @Rollback
    void Return_200_and_ride_when_ordering_a_valid_basic_ride() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        BasicRideCreationDTO dto = IntegrationUtils.getValidBasicRideCreationDto();

        mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driver.name")
                        .value("Travis"))
                .andExpect(jsonPath("$.startAddress")
                        .value("Example starting address 1, Novi Sad"))
                .andExpect(jsonPath("$.distance")
                        .value(5.0));
    }

    @Test
    @DisplayName("Should return 400 when attempting to order a basic ride with a " +
            "non-existent vehicle type [POST] " + URL_PREFIX + "/basic")
    @Rollback
    void Return_404_when_requesting_a_basic_ride_with_a_non_existent_vehicle_type() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        BasicRideCreationDTO dto = IntegrationUtils.getValidBasicRideCreationDto();
        dto.setVehicleType("INVALID_VEHICLE_TYPE");

        mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    @DisplayName("Should return 422 when attempting to order a basic ride " +
            "while having an active ride [POST] " + URL_PREFIX + "/basic")
    @Rollback
    void Return_422_when_ordering_a_basic_ride_while_having_an_active_ride() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        BasicRideCreationDTO dto = IntegrationUtils.getValidBasicRideCreationDto();

        mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(200));

        mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message")
                        .value("Passenger already has an active ride."));

    }

    @Test
    @DisplayName("Should return 402 when attempting to order a basic ride " +
            "without having sufficient funds [POST] " + URL_PREFIX + "/basic")
    @Rollback
    void Return_402_when_ordering_a_basic_ride_without_having_sufficient_funds() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        BasicRideCreationDTO dto = IntegrationUtils.getValidBasicRideCreationDto();
        dto.setDistance(50.0);
        dto.setExpectedTime(4500);

        mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(402));
    }

    @Test
    @DisplayName("Should return 422 when ordering a basic ride " +
            "and a driver is not found [POST] " + URL_PREFIX + "/basic")
    @Rollback
    void Return_422_when_ordering_a_basic_ride_and_a_driver_is_not_found() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        BasicRideCreationDTO dto = IntegrationUtils.getValidBasicRideCreationDto();
        dto.getRoute().setWaypoints(dto.getRoute().getWaypoints().stream().map(wp -> {
            wp.setLat(wp.getLat() + 1);
            wp.setLng(wp.getLng() + 1);
            return wp;
        }).toList());

        mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message")
                        .value("Adequate driver not found."));
    }

    @ParameterizedTest
    @ValueSource(ints = {20, 50, 299})
    @DisplayName("Should return 200 and the created scheduled basic ride [POST] " + URL_PREFIX + "/basic")
    @Rollback
    void Return_200_and_ride_when_ordering_a_valid_scheduled_basic_ride(int delayInMinutes) throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        BasicRideCreationDTO dto = IntegrationUtils.getValidBasicRideCreationDto();
        dto.setDelayInMinutes(delayInMinutes);

        mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startAddress")
                        .value("Example starting address 1, Novi Sad"))
                .andExpect(jsonPath("$.distance")
                        .value(5.0));
    }

    @ParameterizedTest
    @ValueSource(ints = {-100, -1, 301, 1000})
    @DisplayName("Should return 400 when ordering a scheduled basic ride " +
            "and the delay value is outside of the min-max range [0, 300] [POST] " + URL_PREFIX + "/basic")
    @Rollback
    void Return_400_when_ordering_a_scheduled_basic_ride_and_the_delay_value_is_invalid(int delayInMinutes) throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        BasicRideCreationDTO dto = IntegrationUtils.getValidBasicRideCreationDto();
        dto.setDelayInMinutes(delayInMinutes);

        mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(400));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 19})
    @DisplayName("Should return 400 when ordering a scheduled basic ride " +
            "and the delay value is less than 20 [POST] " + URL_PREFIX + "/basic")
    @Rollback
    void Return_400_when_ordering_a_scheduled_basic_ride_and_the_delay_value_is_less_than_20(int delayInMinutes) throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        BasicRideCreationDTO dto = IntegrationUtils.getValidBasicRideCreationDto();
        dto.setDelayInMinutes(delayInMinutes);

        mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message")
                        .value("Reservation must be made at least 20 minutes in advance."));
    }


    // split fare ride
    @Test
    @DisplayName("Should return 200 and the created split fare ride [POST] " + URL_PREFIX + "/split-fare")
    @Rollback
    void Return_200_and_ride_when_ordering_a_valid_split_fare_ride() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        SplitFareRideCreationDTO dto = IntegrationUtils.getValidSplitFareRideCreationDto();
        dto.getUsersToPay().add("passenger2@noemail.com");
        dto.getUsersToPay().add("passenger3@noemail.com");

        mockMvc.perform(post(URL_PREFIX + "/split-fare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startAddress")
                        .value("Example starting address 1, Novi Sad"))
                .andExpect(jsonPath("$.distance")
                        .value(5.0));
    }

    @Test
    @DisplayName("Should return 404 when attempting to order a split fare ride with a " +
            "non-existent vehicle type [POST] " + URL_PREFIX + "/split-fare")
    @Rollback
    void Return_404_when_requesting_a_split_fare_ride_with_a_non_existent_vehicle_type() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        SplitFareRideCreationDTO dto = IntegrationUtils.getValidSplitFareRideCreationDto();
        dto.setVehicleType("INVALID_VEHICLE_TYPE");
        dto.getUsersToPay().add("passenger2@noemail.com");
        dto.getUsersToPay().add("passenger3@noemail.com");

        mockMvc.perform(post(URL_PREFIX + "/split-fare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(404));
    }

    @Test
    @DisplayName("Should return 400 when attempting to order a split fare ride with a " +
            "non-existent co-passenger [POST] " + URL_PREFIX + "/split-fare")
    @Rollback
    void Return_400_when_requesting_a_split_fare_ride_with_a_non_existent_co_passenger() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        SplitFareRideCreationDTO dto = IntegrationUtils.getValidSplitFareRideCreationDto();
        dto.setVehicleType("COUPE");
        dto.getUsersToPay().add("passenger_invalid@noemail.com");
        dto.getUsersToPay().add("passenger3@noemail.com");

        mockMvc.perform(post(URL_PREFIX + "/split-fare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message")
                        .value("A co-passenger's email does not exist in the system."));
    }

    @Test
    @DisplayName("Should return 400 when attempting to order a split fare ride with a " +
            "duplicate co-passenger [POST] " + URL_PREFIX + "/split-fare")
    @Rollback
    void Return_400_when_requesting_a_split_fare_ride_with_a_duplicate_co_passenger() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        SplitFareRideCreationDTO dto = IntegrationUtils.getValidSplitFareRideCreationDto();
        dto.setVehicleType("COUPE");
        dto.getUsersToPay().add("passenger2@noemail.com");
        dto.getUsersToPay().add("passenger2@noemail.com");

        mockMvc.perform(post(URL_PREFIX + "/split-fare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message")
                        .value("Not all linked passengers are distinct."));
    }

    @Test
    @DisplayName("Should return 400 when attempting to order a split fare ride with " +
            "too many passengers [POST] " + URL_PREFIX + "/split-fare")
    @Rollback
    void Return_400_when_requesting_a_split_fare_ride_with_too_many_passengers() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        SplitFareRideCreationDTO dto = IntegrationUtils.getValidSplitFareRideCreationDto();
        dto.setVehicleType("COUPE");
        dto.getUsersToPay().add("passenger2@noemail.com");
        dto.getUsersToPay().add("passenger3@noemail.com");
        dto.getUsersToPay().add("passenger4@noemail.com");
        dto.getUsersToPay().add("passenger5@noemail.com");

        mockMvc.perform(post(URL_PREFIX + "/split-fare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message")
                        .value("Number of passengers exceeds vehicle capacity."));
    }

    @Test
    @DisplayName("Should return 422 when attempting to order a split fare ride when " +
            "a linked passenger has an active ride [POST] " + URL_PREFIX + "/split-fare")
    @Rollback
    void Return_422_when_requesting_a_split_fare_ride_when_a_passenger_has_an_active_ride() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        SplitFareRideCreationDTO dto = IntegrationUtils.getValidSplitFareRideCreationDto();
        dto.setVehicleType("COUPE");
        dto.getUsersToPay().add("passenger2@noemail.com");
        dto.getUsersToPay().add("passenger3@noemail.com");
        dto.getUsersToPay().add("passenger4@noemail.com");

        String passenger2Token = IntegrationUtils.getToken(mockMvc, "passenger2@noemail.com");
        BasicRideCreationDTO dtoBasic = IntegrationUtils.getValidBasicRideCreationDto();

        mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dtoBasic))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passenger2Token))
                .andExpect(status().isOk());

        mockMvc.perform(post(URL_PREFIX + "/split-fare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message")
                        .value("Passenger already has an active ride."));
    }

    @Test
    @DisplayName("Should return 402 when attempting to order a split fare ride without " +
            "sufficient funds [POST] " + URL_PREFIX + "/split-fare")
    @Rollback
    void Return_402_when_requesting_a_split_fare_ride_with_too_many_passengers() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        SplitFareRideCreationDTO dto = IntegrationUtils.getValidSplitFareRideCreationDto();
        dto.setVehicleType("COUPE");
        dto.getUsersToPay().add("passenger2@noemail.com");
        dto.getUsersToPay().add("passenger3@noemail.com");
        dto.setDistance(95.0);
        dto.setExpectedTime(11000);

        mockMvc.perform(post(URL_PREFIX + "/split-fare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(402))
                .andExpect(jsonPath("$.message")
                        .value("Insufficient funds."));
    }

    @ParameterizedTest
    @ValueSource(ints = {-100, -1, 301, 1000})
    @DisplayName("Should return 400 when ordering a scheduled split fare ride " +
            "and the delay value is outside of the min-max range [0, 300] [POST] " + URL_PREFIX + "/split-fare")
    @Rollback
    void Return_400_when_ordering_a_scheduled_split_fare_ride_and_the_delay_value_is_invalid(int delayInMinutes) throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        SplitFareRideCreationDTO dto = IntegrationUtils.getValidSplitFareRideCreationDto();
        dto.setVehicleType("COUPE");
        dto.getUsersToPay().add("passenger2@noemail.com");
        dto.getUsersToPay().add("passenger3@noemail.com");
        dto.setDelayInMinutes(delayInMinutes);

        mockMvc.perform(post(URL_PREFIX + "/split-fare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(400));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 19})
    @DisplayName("Should return 400 when ordering a scheduled split fare ride " +
            "and the delay value is less than 20 [POST] " + URL_PREFIX + "/split-fare")
    @Rollback
    void Return_400_when_ordering_a_scheduled_split_fare_ride_and_the_delay_value_is_less_than_20(int delayInMinutes) throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        SplitFareRideCreationDTO dto = IntegrationUtils.getValidSplitFareRideCreationDto();
        dto.setVehicleType("COUPE");
        dto.getUsersToPay().add("passenger2@noemail.com");
        dto.getUsersToPay().add("passenger3@noemail.com");
        dto.setDelayInMinutes(delayInMinutes);

        mockMvc.perform(post(URL_PREFIX + "/split-fare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message")
                        .value("Reservation must be made at least 20 minutes in advance."));
    }


    // passenger ride rejection/confirmation
    @Test
    @DisplayName("Should throw 404 when a passenger is attempting to reject " +
            "non-existent ride via [PATCH] " + URL_PREFIX + "/reject")
    @Rollback
    void Throw_404_when_attempting_to_reject_non_existent_ride() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        RideIdDTO dto = new RideIdDTO();
        dto.setRideId(111);

        mockMvc.perform(patch(URL_PREFIX + "/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(404));
    }

    @Test
    @DisplayName("Should throw 404 when a passenger is attempting to reject " +
            "another passenger's ride [PATCH] " + URL_PREFIX + "/reject")
    @Rollback
    void Throw_404_when_attempting_to_reject_another_passengers_ride() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        SplitFareRideCreationDTO splitFareRideCreationDto = IntegrationUtils.getValidSplitFareRideCreationDto();
        splitFareRideCreationDto.getUsersToPay().add("passenger2@noemail.com");
        String passenger5Token = IntegrationUtils.getToken(mockMvc, "passenger5@noemail.com");

        MvcResult res =  mockMvc.perform(post(URL_PREFIX + "/split-fare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(splitFareRideCreationDto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().isOk()).andReturn();

        String rideResponse = res.getResponse().getContentAsString();
        Integer id = JsonPath.parse(rideResponse).read("$.id");
        RideIdDTO rideIdDTO = new RideIdDTO();
        rideIdDTO.setRideId(id);

        mockMvc.perform(patch(URL_PREFIX + "/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rideIdDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passenger5Token))
                .andExpect(status().is(404));
    }

    @Test
    @DisplayName("Should return 200 when a passenger rejection is valid [PATCH] " + URL_PREFIX + "/reject")
    @Rollback
    void Return_200_when_rejection_is_valid() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        SplitFareRideCreationDTO splitFareRideCreationDto = IntegrationUtils.getValidSplitFareRideCreationDto();
        splitFareRideCreationDto.getUsersToPay().add("passenger2@noemail.com");
        String passenger2Token = IntegrationUtils.getToken(mockMvc, "passenger2@noemail.com");

        MvcResult res =  mockMvc.perform(post(URL_PREFIX + "/split-fare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(splitFareRideCreationDto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().isOk()).andReturn();

        String rideResponse = res.getResponse().getContentAsString();
        Integer id = JsonPath.parse(rideResponse).read("$.id");
        RideIdDTO rideIdDTO = new RideIdDTO();
        rideIdDTO.setRideId(id);

        mockMvc.perform(patch(URL_PREFIX + "/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rideIdDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passenger2Token))
                .andExpect(status().is(200))
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Should throw 404 when attempting to confirm " +
            "non-existent ride via [PATCH] " + URL_PREFIX + "/confirm")
    @Rollback
    void Throw_404_when_attempting_to_confirm_non_existent_ride() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        RideIdDTO dto = new RideIdDTO();
        dto.setRideId(111);

        mockMvc.perform(patch(URL_PREFIX + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(404));
    }

    @Test
    @DisplayName("Should throw 404 when a passenger is attempting to confirm " +
            "another passenger's ride [PATCH] " + URL_PREFIX + "/confirm")
    @Rollback
    void Throw_404_when_attempting_to_confirm_another_passengers_ride() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        SplitFareRideCreationDTO splitFareRideCreationDto = IntegrationUtils.getValidSplitFareRideCreationDto();
        splitFareRideCreationDto.getUsersToPay().add("passenger2@noemail.com");
        String passenger5Token = IntegrationUtils.getToken(mockMvc, "passenger5@noemail.com");

        MvcResult res =  mockMvc.perform(post(URL_PREFIX + "/split-fare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(splitFareRideCreationDto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().isOk()).andReturn();

        String rideResponse = res.getResponse().getContentAsString();
        Integer id = JsonPath.parse(rideResponse).read("$.id");
        RideIdDTO rideIdDTO = new RideIdDTO();
        rideIdDTO.setRideId(id);

        mockMvc.perform(patch(URL_PREFIX + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rideIdDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passenger5Token))
                .andExpect(status().is(404));
    }

    @Test
    @DisplayName("Should return 200 when attempting to confirm a ride without sufficient " +
            "funds [PATCH] " + URL_PREFIX + "/confirm")
    @Rollback
    void Return_402_when_attempting_to_confirm_a_ride_without_sufficient_funds() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        SplitFareRideCreationDTO splitFareRideCreationDto = IntegrationUtils.getValidSplitFareRideCreationDto();
        splitFareRideCreationDto.getUsersToPay().add("passenger2@noemail.com");
        String passenger2Token = IntegrationUtils.getToken(mockMvc, "passenger2@noemail.com");

        MvcResult res =  mockMvc.perform(post(URL_PREFIX + "/split-fare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(splitFareRideCreationDto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().isOk()).andReturn();

        String rideResponse = res.getResponse().getContentAsString();
        Integer id = JsonPath.parse(rideResponse).read("$.id");
        RideIdDTO rideIdDTO = new RideIdDTO();
        rideIdDTO.setRideId(id);

        mockMvc.perform(patch(URL_PREFIX + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rideIdDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passenger2Token))
                .andExpect(status().is(200))
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Should return 200 when a passenger confirmation is valid [PATCH] " + URL_PREFIX + "/confirm")
    @Rollback
    void Return_200_when_confirmation_is_valid() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        SplitFareRideCreationDTO splitFareRideCreationDto = IntegrationUtils.getValidSplitFareRideCreationDto();
        splitFareRideCreationDto.getUsersToPay().add("passenger5@noemail.com");
        String passenger5Token = IntegrationUtils.getToken(mockMvc, "passenger5@noemail.com");

        MvcResult res =  mockMvc.perform(post(URL_PREFIX + "/split-fare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(splitFareRideCreationDto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().isOk()).andReturn();

        String rideResponse = res.getResponse().getContentAsString();
        Integer id = JsonPath.parse(rideResponse).read("$.id");
        RideIdDTO rideIdDTO = new RideIdDTO();
        rideIdDTO.setRideId(id);

        mockMvc.perform(patch(URL_PREFIX + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rideIdDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passenger5Token))
                .andExpect(status().is(402))
                .andExpect(jsonPath("$.message")
                        .value("Insufficient funds."));
    }

    @Test
    @DisplayName("Should return 200 and false when attempting to reject after first confirming [PATCH] " + URL_PREFIX + "/reject")
    @Rollback
    void Return_false_when_attempting_to_reject_after_passenger_has_confirmed() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        SplitFareRideCreationDTO splitFareRideCreationDto = IntegrationUtils.getValidSplitFareRideCreationDto();
        splitFareRideCreationDto.getUsersToPay().add("passenger2@noemail.com");
        String passenger2Token = IntegrationUtils.getToken(mockMvc, "passenger2@noemail.com");

        MvcResult res =  mockMvc.perform(post(URL_PREFIX + "/split-fare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(splitFareRideCreationDto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().isOk()).andReturn();

        String rideResponse = res.getResponse().getContentAsString();
        Integer id = JsonPath.parse(rideResponse).read("$.id");
        RideIdDTO rideIdDTO = new RideIdDTO();
        rideIdDTO.setRideId(id);

        mockMvc.perform(patch(URL_PREFIX + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rideIdDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passenger2Token))
                .andExpect(status().is(200))
                .andExpect(content().string("true"));

        mockMvc.perform(patch(URL_PREFIX + "/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rideIdDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passenger2Token))
                .andExpect(status().is(200))
                .andExpect(content().string("false"));
    }


    // driver inconsistency report
    @Test
    @DisplayName("Should return 404 when attempting to report an inconsistency on a non-existent ride " +
            "[PATCH] " + URL_PREFIX + "/inconsistency")
    @Rollback
    void Return_404_when_reporting_an_inconsistency_on_a_non_existing_ride() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        RideIdDTO dto = new RideIdDTO();
        dto.setRideId(111);

        mockMvc.perform(patch(URL_PREFIX + "/inconsistency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(404));
    }

    @Test
    @DisplayName("Should return 400 when attempting to report an inconsistency on a ride that " +
            "does not belong to the passenger [PATCH] " + URL_PREFIX + "/inconsistency")
    @Rollback
    void Return_400_when_reporting_an_inconsistency_on_a_ride_that_does_not_belong_to_the_passenger() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        BasicRideCreationDTO splitFareRideCreationDto = IntegrationUtils.getValidBasicRideCreationDto();

        MvcResult res =  mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(splitFareRideCreationDto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().isOk()).andReturn();

        String rideResponse = res.getResponse().getContentAsString();
        Integer id = JsonPath.parse(rideResponse).read("$.id");
        RideIdDTO rideIdDTO = new RideIdDTO();
        rideIdDTO.setRideId(id);

        String passenger2Token = IntegrationUtils.getToken(mockMvc, "passenger2@noemail.com");

        mockMvc.perform(patch(URL_PREFIX + "/inconsistency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rideIdDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passenger2Token))
                .andExpect(status().is(400));
    }

    @Test
    @DisplayName("Should return 200 and true when reporting an inconsistency [PATCH] " + URL_PREFIX + "/inconsistency")
    @Rollback
    void Return_200_and_true_when_reporting_inconsistency() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        BasicRideCreationDTO splitFareRideCreationDto = IntegrationUtils.getValidBasicRideCreationDto();

        MvcResult res =  mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(splitFareRideCreationDto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().isOk()).andReturn();

        String rideResponse = res.getResponse().getContentAsString();
        Integer id = JsonPath.parse(rideResponse).read("$.id");
        RideIdDTO rideIdDTO = new RideIdDTO();
        rideIdDTO.setRideId(id);

        mockMvc.perform(patch(URL_PREFIX + "/inconsistency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rideIdDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().is(200))
                .andExpect(content().string("true"));
    }


    // begin ride
    @Test
    @DisplayName("Should return 404 when attempting to start a non-existent ride " +
            "[PATCH] " + URL_PREFIX + "/begin")
    @Rollback
    void Return_404_when_attempting_to_start_a_non_existing_ride() throws Exception {
        String driverToken = IntegrationUtils.getToken(mockMvc, "driver1@noemail.com");
        RideIdDTO dto = new RideIdDTO();
        dto.setRideId(111);

        mockMvc.perform(patch(URL_PREFIX + "/begin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + driverToken))
                .andExpect(status().is(404));
    }

    @Test
    @DisplayName("Should return 200 and true when ride started successfully " +
            "[PATCH] " + URL_PREFIX + "/begin")
    @Rollback
    void Return_200_and_true_when_a_ride_is_started_successfully() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        BasicRideCreationDTO rideCreationDTO = IntegrationUtils.getValidBasicRideCreationDto();

        MvcResult res = mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rideCreationDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().isOk()).andReturn();

        String rideResponse = res.getResponse().getContentAsString();
        Integer id = JsonPath.parse(rideResponse).read("$.id");
        RideIdDTO rideIdDTO = new RideIdDTO();
        rideIdDTO.setRideId(id);

        String driverToken = IntegrationUtils.getToken(mockMvc, "driver1@noemail.com");
        RideIdDTO dto = new RideIdDTO();
        dto.setRideId(id);

        mockMvc.perform(patch(URL_PREFIX + "/begin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + driverToken))
                .andExpect(status().is(200))
                .andExpect(content().string("true"));
    }


    // complete ride
    @Test
    @DisplayName("Should return 404 when attempting to complete a non-existent ride " +
            "[PATCH] " + URL_PREFIX + "/complete")
    @Rollback
    void Return_404_when_attempting_to_complete_a_non_existing_ride() throws Exception {
        String driverToken = IntegrationUtils.getToken(mockMvc, "driver1@noemail.com");
        RideIdDTO dto = new RideIdDTO();
        dto.setRideId(111);

        mockMvc.perform(patch(URL_PREFIX + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + driverToken))
                .andExpect(status().is(404));
    }

    @Test
    @DisplayName("Should return 200 and true when ride started and completed successfully " +
            "[PATCH] " + URL_PREFIX + "/complete")
    @Rollback
    void Return_200_and_true_when_a_ride_is_completed_successfully() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        BasicRideCreationDTO rideCreationDTO = IntegrationUtils.getValidBasicRideCreationDto();

        MvcResult res = mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rideCreationDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andReturn();

        String rideResponse = res.getResponse().getContentAsString();
        Integer id = JsonPath.parse(rideResponse).read("$.id");
        RideIdDTO rideIdDTO = new RideIdDTO();
        rideIdDTO.setRideId(id);

        String driverToken = IntegrationUtils.getToken(mockMvc, "driver1@noemail.com");
        RideIdDTO dto = new RideIdDTO();
        dto.setRideId(id);

        mockMvc.perform(patch(URL_PREFIX + "/begin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + driverToken))
                .andExpect(status().is(200))
                .andExpect(content().string("true"));


        mockMvc.perform(patch(URL_PREFIX + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + driverToken))
                .andExpect(status().is(200))
                .andExpect(content().string("true"));
    }


    // driver reject ride
    @Test
    @DisplayName("Should return 404 when a driver is attempting to reject a non-existent ride " +
            "[PATCH] " + URL_PREFIX + "/driver-rejection")
    @Rollback
    void Return_404_when_a_driver_is_attempting_to_reject_a_non_existing_ride() throws Exception {
        String driverToken = IntegrationUtils.getToken(mockMvc, "driver1@noemail.com");
        DriverRideRejectionCreationDTO dto = new DriverRideRejectionCreationDTO();
        dto.setRideId(111);

        mockMvc.perform(patch(URL_PREFIX + "/driver-rejection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + driverToken))
                .andExpect(status().is(404));
    }

    @Test
    @DisplayName("Should return 200 and true when a driver successfully submits a rejection request " +
            "[PATCH] " + URL_PREFIX + "/driver-rejection")
    @Rollback
    void Return_200_and_true_when_a_driver_ride_rejection_is_successfully_submitted() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        BasicRideCreationDTO rideCreationDTO = IntegrationUtils.getValidBasicRideCreationDto();

        MvcResult res = mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rideCreationDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().isOk()).andReturn();

        String rideResponse = res.getResponse().getContentAsString();
        Integer id = JsonPath.parse(rideResponse).read("$.id");
        RideIdDTO rideIdDTO = new RideIdDTO();
        rideIdDTO.setRideId(id);

        String driverToken = IntegrationUtils.getToken(mockMvc, "driver1@noemail.com");
        DriverRideRejectionCreationDTO dto = new DriverRideRejectionCreationDTO();
        dto.setReason("Reason for rejection.");
        dto.setRideId(id);

        mockMvc.perform(patch(URL_PREFIX + "/driver-rejection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + driverToken))
                .andExpect(status().is(200))
                .andExpect(content().string("true"));
    }


    // get driver rejection requests
    @Test
    @DisplayName("Should return 200 and an empty list when there are no driver ride rejection " +
            "requests [GET] " + URL_PREFIX + "/rejection-requests")
    @Rollback
    void Return_200_and_an_empty_list_when_there_are_no_driver_ride_rejection_requests() throws Exception {
        String adminToken = IntegrationUtils.getToken(mockMvc, "admin1@noemail.com");

        mockMvc.perform(get(URL_PREFIX + "/rejection-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return 200 and a list of driver ride rejection " +
            "requests [GET] " + URL_PREFIX + "/rejection-requests")
    @Rollback
    void Return_200_and_a_list_of_driver_ride_rejection_requests() throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        BasicRideCreationDTO rideCreationDTO = IntegrationUtils.getValidBasicRideCreationDto();

        MvcResult res = mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rideCreationDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().isOk()).andReturn();

        String rideResponse = res.getResponse().getContentAsString();
        Integer id = JsonPath.parse(rideResponse).read("$.id");
        DriverRideRejectionCreationDTO rejectionCreationDTO = new DriverRideRejectionCreationDTO();
        rejectionCreationDTO.setReason("Reason for rejection.");
        rejectionCreationDTO.setRideId(id);

        String driverToken = IntegrationUtils.getToken(mockMvc, "driver1@noemail.com");

        mockMvc.perform(patch(URL_PREFIX + "/driver-rejection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rejectionCreationDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + driverToken))
                .andExpect(status().is(200))
                .andExpect(content().string("true"));


        String adminToken = IntegrationUtils.getToken(mockMvc, "admin1@noemail.com");

        mockMvc.perform(get(URL_PREFIX + "/rejection-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[*].reason")
                        .value("Reason for rejection."))
                .andExpect(jsonPath("$.[*].*.name")
                        .value("Travis"));
    }


    // accept driver ride rejection
    @Test
    @DisplayName("Should return 404 when attempting to accept a driver rejection request a non-existent ride " +
            "[PATCH] " + URL_PREFIX + "/driver-rejection-verdict")
    @Rollback
    void Return_404_when_a_attempting_to_accept_a_driver_rejection_for_a_non_existent_ride() throws Exception {
        String adminToken = IntegrationUtils.getToken(mockMvc, "admin1@noemail.com");
        RideIdDTO dto = new RideIdDTO();
        dto.setRideId(111);

        mockMvc.perform(patch(URL_PREFIX + "/driver-rejection-verdict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().is(404));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    @DisplayName("Should return 200 and true when a driver rejection request is successfully responded to " +
            "[PATCH] " + URL_PREFIX + "/driver-rejection-verdict")
    @Rollback
    void Return_200_and_true_when_a_driver_ride_rejection_is_successfully_responded_to(boolean isAccepted) throws Exception {
        String passengerToken = IntegrationUtils.getToken(mockMvc, "passenger1@noemail.com");
        BasicRideCreationDTO rideCreationDTO = IntegrationUtils.getValidBasicRideCreationDto();

        MvcResult res = mockMvc.perform(post(URL_PREFIX + "/basic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rideCreationDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + passengerToken))
                .andExpect(status().isOk()).andReturn();

        String rideResponse = res.getResponse().getContentAsString();
        Integer id = JsonPath.parse(rideResponse).read("$.id");
        DriverRideRejectionCreationDTO rejectionCreationDTO = new DriverRideRejectionCreationDTO();
        rejectionCreationDTO.setReason("Reason for rejection.");
        rejectionCreationDTO.setRideId(id);

        String driverToken = IntegrationUtils.getToken(mockMvc, "driver1@noemail.com");

        mockMvc.perform(patch(URL_PREFIX + "/driver-rejection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(rejectionCreationDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + driverToken))
                .andExpect(status().is(200))
                .andExpect(content().string("true"));

        String adminToken = IntegrationUtils.getToken(mockMvc, "admin1@noemail.com");
        DriverRideRejectionVerdictCreationDTO verdictCreationDto = new DriverRideRejectionVerdictCreationDTO();
        verdictCreationDto.setRideId(id);
        verdictCreationDto.setAccepted(isAccepted);

        mockMvc.perform(patch(URL_PREFIX + "/driver-rejection-verdict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(verdictCreationDto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().is(200))
                .andExpect(content().string("true"));
    }
}
