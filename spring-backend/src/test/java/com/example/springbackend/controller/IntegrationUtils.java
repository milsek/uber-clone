package com.example.springbackend.controller;

import com.example.springbackend.dto.creation.BasicRideCreationDTO;
import com.example.springbackend.dto.creation.CoordinatesCreationDTO;
import com.example.springbackend.dto.creation.RouteCreationDTO;
import com.example.springbackend.dto.creation.SplitFareRideCreationDTO;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class IntegrationUtils {

    public static String getToken(MockMvc mockMvc, String username) throws Exception {
        String payload = "{ \"username\": \"" + username + "\", \"password\": \"cascaded\"}";
        String token = mockMvc.perform(post("/api/auth/custom-login")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("\"}", "")
                .substring(16)
                .split(",")[0];
        return token.substring(0, token.length() - 1);
    }

    public static BasicRideCreationDTO getValidBasicRideCreationDto() {
        BasicRideCreationDTO dto = new BasicRideCreationDTO();
        dto.setDistance(5.0);
        dto.setExpectedTime(400);
        dto.setBabySeat(false);
        dto.setPetFriendly(false);
        dto.setStartAddress("Example starting address 1, Subotica");
        dto.setDestinationAddress("Example destination address 1, Subotica");
        dto.setVehicleType("COUPE");
        RouteCreationDTO routeDto = new RouteCreationDTO();
        CoordinatesCreationDTO coordinatesDto1 = new CoordinatesCreationDTO();
        coordinatesDto1.setLat(46.095263);
        coordinatesDto1.setLng(19.654779);
        CoordinatesCreationDTO coordinatesDto2 = new CoordinatesCreationDTO();
        coordinatesDto2.setLat(46.104840);
        coordinatesDto2.setLng(19.658408);
        CoordinatesCreationDTO waypointsDto1 = new CoordinatesCreationDTO();
        waypointsDto1.setLat(46.095263);
        waypointsDto1.setLng(19.654779);
        CoordinatesCreationDTO waypointsDto2 = new CoordinatesCreationDTO();
        waypointsDto2.setLat(46.104840);
        waypointsDto2.setLng(19.658408);
        List<CoordinatesCreationDTO> coordinatesDtoList = new ArrayList<>();
        coordinatesDtoList.add(coordinatesDto1);
        coordinatesDtoList.add(coordinatesDto2);
        List<CoordinatesCreationDTO> waypointsDtoList = new ArrayList<>();
        waypointsDtoList.add(waypointsDto1);
        waypointsDtoList.add(waypointsDto2);
        routeDto.setCoordinates(coordinatesDtoList);
        routeDto.setWaypoints(waypointsDtoList);
        dto.setRoute(routeDto);
        dto.setDelayInMinutes(0);
        return dto;
    }

    public static SplitFareRideCreationDTO getValidSplitFareRideCreationDto() {
        SplitFareRideCreationDTO dto = new SplitFareRideCreationDTO();

        dto.setDistance(5.0);
        dto.setExpectedTime(400);
        dto.setBabySeat(false);
        dto.setPetFriendly(false);
        dto.setStartAddress("Example starting address 1, Subotica");
        dto.setDestinationAddress("Example destination address 1, Subotica");
        dto.setVehicleType("COUPE");
        RouteCreationDTO routeDto = new RouteCreationDTO();;
        CoordinatesCreationDTO coordinatesDto1 = new CoordinatesCreationDTO();
        coordinatesDto1.setLat(46.095263);
        coordinatesDto1.setLng(19.654779);
        CoordinatesCreationDTO coordinatesDto2 = new CoordinatesCreationDTO();
        coordinatesDto2.setLat(46.104840);
        coordinatesDto2.setLng(19.658408);
        CoordinatesCreationDTO waypointsDto1 = new CoordinatesCreationDTO();
        waypointsDto1.setLat(46.095263);
        waypointsDto1.setLng(19.654779);
        CoordinatesCreationDTO waypointsDto2 = new CoordinatesCreationDTO();
        waypointsDto2.setLat(46.104840);
        waypointsDto2.setLng(19.658408);
        List<CoordinatesCreationDTO> coordinatesDtoList = new ArrayList<>();
        coordinatesDtoList.add(coordinatesDto1);
        coordinatesDtoList.add(coordinatesDto2);
        List<CoordinatesCreationDTO> waypointsDtoList = new ArrayList<>();
        waypointsDtoList.add(waypointsDto1);
        waypointsDtoList.add(waypointsDto2);
        routeDto.setCoordinates(coordinatesDtoList);
        routeDto.setWaypoints(waypointsDtoList);
        dto.setRoute(routeDto);
        dto.setDelayInMinutes(0);
        dto.setUsersToPay(new ArrayList<>());
        return dto;
    }
}
