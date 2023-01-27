package com.example.springbackend.repository;

import com.example.springbackend.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PassengerRideRepository extends JpaRepository<PassengerRide, Integer> {
    @Query("SELECT pr FROM PassengerRide pr WHERE " +
            "pr.passenger = :passenger AND " +
            "pr.ride.status != com.example.springbackend.model.RideStatus.CANCELLED AND " +
            "pr.ride.status != com.example.springbackend.model.RideStatus.COMPLETED")
    Optional<PassengerRide> getCurrentPassengerRide(@Param("passenger") Passenger passenger);

    @Query("SELECT pr FROM PassengerRide pr WHERE " +
            "pr.passenger.username in :usernames AND " +
            "pr.ride.status != com.example.springbackend.model.RideStatus.CANCELLED AND " +
            "pr.ride.status != com.example.springbackend.model.RideStatus.COMPLETED")
    List<PassengerRide> getCurrentPassengerRidesByUsername(@Param("usernames") List<String> usernames);

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

    List<PassengerRide> findByRide(Ride ride);

    @Query("SELECT pr.passenger.username FROM PassengerRide pr WHERE " +
            "pr.ride.id = :rideId")
    List<String> getPassengersForRide(@Param("rideId") Integer rideId);

    @Query("SELECT pr FROM PassengerRide pr WHERE " +
            "pr.ride.route.id = :routeId AND pr.passenger.username = :username" )
    Optional<PassengerRide> findByRideRouteAndUsername(@Param("routeId") Integer routeId, @Param("username") String username);

    @Query("SELECT pr FROM PassengerRide pr WHERE pr.ride.driver = :driver")
    Page<PassengerRide> findByDriver(@Param("driver") Driver driver, Pageable pageable);
}

