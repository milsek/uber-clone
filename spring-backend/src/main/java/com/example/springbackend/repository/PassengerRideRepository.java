package com.example.springbackend.repository;

import com.example.springbackend.model.Passenger;
import com.example.springbackend.model.PassengerRide;
import com.example.springbackend.model.Ride;
import io.swagger.models.auth.In;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PassengerRideRepository extends JpaRepository<PassengerRide, Integer> {
    @Query("SELECT pr.ride FROM PassengerRide pr WHERE " +
            "pr.passenger = :passenger AND pr.ride.rejected = false " +
            "AND pr.ride.endTime is null")
    Optional<Ride> getCurrentRide(@Param("passenger") Passenger passenger);

    Optional<PassengerRide> findByRideAndPassengerUsername(Ride ride, String username);
    Optional<PassengerRide> findByRideIdAndPassengerUsername(Integer rideId, String username);
    List<PassengerRide> findByRideId(Integer rideId);

    Page<PassengerRide> findByPassengerUsername(String username, Pageable paging);


    @Query(value = "SELECT  cast(pr.ride.startTime as date), SUM (pr.ride.distance) \n" +
            "            FROM \n" +
            "    PassengerRide pr inner join pr.ride ride where cast(pr.ride.startTime as date) >= ?1 and" +
            " cast(pr.ride.startTime as date) <= ?2 and pr.passenger.username = ?3" +
            "      group by cast(pr.ride.startTime as date) order by" +
            " cast(pr.ride.startTime as date)")
    List<Object[]> getPassengerDistanceReport(Date startDate, Date endDate, String username);
    @Query(value = "SELECT  cast(pr.ride.startTime as date),COUNT (pr.ride) \n" +
            "            FROM \n" +
            "    PassengerRide pr inner join pr.ride ride where cast(pr.ride.startTime as date) >= ?1 and" +
            " cast(pr.ride.startTime as date) <= ?2 and pr.passenger.username = ?3" +
            "      group by cast(pr.ride.startTime as date) order by" +
            " cast(pr.ride.startTime as date)")
    List<Object[]> getPassengerRidesReport(Date startDate, Date endDate, String username);
    @Query(value = "SELECT  cast(pr.ride.startTime as date), SUM(pr.fare)\n" +
            "            FROM \n" +
            "    PassengerRide pr inner join pr.ride ride where cast(pr.ride.startTime as date) >= ?1 and" +
            " cast(pr.ride.startTime as date) <= ?2 and pr.passenger.username = ?3" +
            "      group by cast(pr.ride.startTime as date) order by" +
            " cast(pr.ride.startTime as date)")
    List<Object[]> getPassengersMoneyReport(Date startDate, Date endDate, String username);


    @Query(value = "SELECT  cast(pr.ride.startTime as date), SUM (pr.ride.distance) \n" +
            "            FROM \n" +
            "    PassengerRide pr inner join pr.ride ride where cast(pr.ride.startTime as date) >= ?1 and" +
            " cast(pr.ride.startTime as date) <= ?2" +
            "      group by cast(pr.ride.startTime as date) order by" +
            " cast(pr.ride.startTime as date)")
    List<Object[]> getAllPassengersDistanceReport(Date startDate, Date endDate);
    @Query(value = "SELECT  cast(pr.ride.startTime as date),COUNT (pr.ride) \n" +
            "            FROM \n" +
            "    PassengerRide pr inner join pr.ride ride where cast(pr.ride.startTime as date) >= ?1 and" +
            " cast(pr.ride.startTime as date) <= ?2" +
            "      group by cast(pr.ride.startTime as date) order by" +
            " cast(pr.ride.startTime as date)")
    List<Object[]> getAllPassengersRidesReport(Date startDate, Date endDate);
    @Query(value = "SELECT  cast(pr.ride.startTime as date), SUM(pr.fare)\n" +
            "            FROM \n" +
            "    PassengerRide pr inner join pr.ride ride where cast(pr.ride.startTime as date) >= ?1 and" +
            " cast(pr.ride.startTime as date) <= ?2" +
            "      group by cast(pr.ride.startTime as date) order by" +
            " cast(pr.ride.startTime as date)")
    List<Object[]> getAllPassengersMoneyReport(Date startDate, Date endDate);

    @Query("SELECT pr.passenger.username FROM PassengerRide pr WHERE " +
            "pr.ride.id = :rideId")
    List<String> getPassengersForRide(@Param("rideId") Integer rideId);
}

