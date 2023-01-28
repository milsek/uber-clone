package com.example.springbackend.controller;

import com.example.springbackend.dto.creation.*;
import com.example.springbackend.dto.display.*;
import com.example.springbackend.dto.display.ReportDisplayDTO;
import com.example.springbackend.model.helpClasses.ReportParameter;
import com.example.springbackend.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/api/rides", produces = MediaType.APPLICATION_JSON_VALUE)
public class RideController {
    @Autowired
    RideService rideService;

    @PostMapping("/basic")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<RideSimpleDisplayDTO> orderBasicRide(@Valid @RequestBody BasicRideCreationDTO dto,
                                                               Authentication auth) {
        return ResponseEntity.ok(rideService.orderBasicRide(dto, auth));
    }

    @PostMapping("/split-fare")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Boolean> orderSplitFareRide(@Valid @RequestBody SplitFareRideCreationDTO dto,
                                                      Authentication auth) {
        return ResponseEntity.ok(rideService.orderSplitFareRide(dto, auth));
    }

    @PatchMapping("/confirm")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Boolean> confirmRide(@Valid @RequestBody RideIdDTO dto, Authentication auth) {
        return ResponseEntity.ok(rideService.confirmRide(dto, auth));
    }

    @PatchMapping("/reject")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Boolean> rejectRide(@Valid @RequestBody RideIdDTO dto, Authentication auth) {
        return ResponseEntity.ok(rideService.rejectRide(dto, auth));
    }

    @PatchMapping("/inconsistency")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Boolean> reportInconsistency(@Valid @RequestBody RideIdDTO dto, Authentication auth) {
        return ResponseEntity.ok(rideService.reportInconsistency(dto, auth));
    }

    @PatchMapping("/begin")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Boolean> beginRide(@Valid @RequestBody RideIdDTO dto, Authentication auth) {
        return ResponseEntity.ok(rideService.beginRide(dto, auth));
    }

    @PatchMapping("/complete")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Boolean> completeRide(@Valid @RequestBody RideIdDTO dto, Authentication auth) {
        return ResponseEntity.ok(rideService.completeRide(dto, auth));
    }

    @PatchMapping("/driver-rejection")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Boolean> driverRejectRide(@Valid @RequestBody DriverRideRejectionCreationDTO dto,
                                                    Authentication auth) {
        return ResponseEntity.ok(rideService.driverRejectRide(dto, auth));
    }

    @GetMapping("/rejection-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DriverRideRejectionDisplayDTO>> getDriverRideRejectionRequests(Authentication auth) {
        return ResponseEntity.ok(rideService.getDriverRideRejectionRequests(auth));
    }

    @PatchMapping("/driver-rejection-verdict")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> acceptDriverRideRejection(@Valid @RequestBody DriverRideRejectionVerdictCreationDTO dto,
                                                             Authentication auth) {
        return ResponseEntity.ok(rideService.acceptDriverRideRejection(dto, auth));
    }


    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    public Page<RideHistoryDisplayDTO> getRideHistory(@Valid @RequestParam(value="page") Integer page,
                                                      @RequestParam(value="amount") Integer amount,
                                                      @RequestParam(value="sortBy") String sortBy,
                                                      @RequestParam(value="username") String username,
                                                      Authentication auth){
        Pageable paging = PageRequest.of(page, amount, Sort.by(sortBy));
        return rideService.getRideHistory(auth, paging, username);
    }

    @GetMapping("/driver-history")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public Page<RideHistoryDisplayDTO> getDriverRideHistory(@Valid @RequestParam(value="page") Integer page,
                                                            @RequestParam(value="amount") Integer amount,
                                                            @RequestParam(value="sortBy") String sortBy,
                                                            @RequestParam(value="username") String username,
                                                            Authentication auth){
        Pageable paging = PageRequest.of(page, amount, Sort.by(sortBy));
        return rideService.getDriverRideHistory(auth, paging, username);
    }

    @GetMapping("/detailed-ride-history-passenger")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    public DetailedRideHistoryPassengerDTO detailedRideHistoryPassenger(@Valid @RequestParam Integer rideId,
                                                                        Authentication authentication){
        return rideService.detailedRideHistoryPassenger(rideId, authentication);
    }

    @GetMapping("/detailed-ride-history-driver")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public DetailedRideHistoryDriverDTO detailedRideHistoryDriver(@Valid @RequestParam Integer rideId,
                                                                  Authentication authentication){
        return rideService.detailedRideHistoryDriver(rideId, authentication);
    }

    @PostMapping("/reviews")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Boolean> leaveReview(@Valid @RequestBody ReviewCreationDTO reviewCreationDTO,
                                               Authentication authentication ){
        return ResponseEntity.ok(rideService.leaveReview(reviewCreationDTO, authentication));
    }

    @GetMapping("/generate-report-passenger")
    @PreAuthorize("hasRole('PASSENGER')")
    public ReportDisplayDTO generateReportPassenger(@RequestParam String startDate, @RequestParam String endDate,
                                                    @RequestParam ReportParameter reportParameter,
                                                    Authentication authentication ){
        return rideService.generateReportPassenger(startDate, endDate, reportParameter, authentication);
    }

    @GetMapping("/generate-report-driver")
    @PreAuthorize("hasRole('DRIVER')")
    public ReportDisplayDTO generateReportDriver(@RequestParam String startDate, @RequestParam String endDate,
                                                 @RequestParam ReportParameter reportParameter,
                                                 Authentication authentication ){
        return rideService.generateReportDriver(startDate, endDate, reportParameter, authentication);
    }

    @GetMapping("/generate-report-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ReportDisplayDTO generateReportAdmin(@RequestParam String startDate, @RequestParam String endDate,
                                                @RequestParam ReportParameter reportParameter,
                                                @RequestParam String type,
                                                Authentication authentication ){
        return rideService.generateReportAdmin(startDate, endDate, reportParameter, type, authentication);
    }
}
