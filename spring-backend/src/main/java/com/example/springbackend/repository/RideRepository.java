package com.example.springbackend.repository;

import com.example.springbackend.model.Ride;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, Integer> {


    Page<Ride> findByDriverUsername(String username, Pageable paging);

    @Query(value = "SELECT  cast(ride.startTime as date), SUM(ride.distance)\n" +
            "            FROM \n" +
            "    Ride ride where cast(ride.startTime as date) >= ?1 and" +
            " cast(ride.startTime as date) <= ?2 and ride.driver.username = ?3" +
            "      group by cast(ride.startTime as date) order by" +
            " cast(ride.startTime as date)")
    List<Object[]> getDriverDistanceReport(Date startDate, Date endDate, String username);
    @Query(value = "SELECT  cast(ride.startTime as date), count (ride)\n" +
            "            FROM \n" +
            "    Ride ride where cast(ride.startTime as date) >= ?1 and" +
            " cast(ride.startTime as date) <= ?2 and ride.driver.username = ?3" +
            "      group by cast(ride.startTime as date) order by" +
            " cast(ride.startTime as date)")
    List<Object[]> getDriverRidesReport(Date startDate, Date endDate, String username);
    @Query(value = "SELECT  cast(ride.startTime as date), SUM(ride.price)\n" +
            "            FROM \n" +
            "    Ride ride where cast(ride.startTime as date) >= ?1 and" +
            " cast(ride.startTime as date) <= ?2 and ride.driver.username = ?3" +
            "      group by cast(ride.startTime as date) order by" +
            " cast(ride.startTime as date)")
    List<Object[]> getDriverMoneyReport(Date startDate, Date endDate, String username);

    @Query(value = "SELECT  cast(ride.startTime as date), SUM(ride.distance)\n" +
            "            FROM \n" +
            "    Ride ride where cast(ride.startTime as date) >= ?1 and" +
            " cast(ride.startTime as date) <= ?2" +
            "      group by cast(ride.startTime as date) order by" +
            " cast(ride.startTime as date)")
    List<Object[]> getAllDriversDistanceReport(Date startDate, Date endDate);
    @Query(value = "SELECT  cast(ride.startTime as date), count (ride)\n" +
            "            FROM \n" +
            "    Ride ride where cast(ride.startTime as date) >= ?1 and" +
            " cast(ride.startTime as date) <= ?2" +
            "      group by cast(ride.startTime as date) order by" +
            " cast(ride.startTime as date)")
    List<Object[]> getAllDriversRidesReport(Date startDate, Date endDate);
    @Query(value = "SELECT  cast(ride.startTime as date), SUM(ride.price)\n" +
            "            FROM \n" +
            "    Ride ride where cast(ride.startTime as date) >= ?1 and" +
            " cast(ride.startTime as date) <= ?2" +
            "      group by cast(ride.startTime as date) order by" +
            " cast(ride.startTime as date)")
    List<Object[]> getAllDriversMoneyReport(Date startDate, Date endDate);

    @Query(value = "SELECT r FROM Ride r WHERE r.driverRejectionReason is not null AND " +
            "r.status = com.example.springbackend.model.RideStatus.DRIVER_ARRIVING")
    List<Ride> getRidesPendingRejection();
}
