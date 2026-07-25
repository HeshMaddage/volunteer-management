package com.volunteer.management.hourslog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HoursLogRepository extends JpaRepository<HoursLog, UUID> {

    @Query("""
            SELECT h FROM HoursLog h
            WHERE h.registration.volunteer.id = :volunteerId
            ORDER BY h.loggedAt DESC
            """)
    List<HoursLog> findByVolunteerId(@Param("volunteerId") UUID volunteerId);

    @Query("""
            SELECT COALESCE(SUM(h.hours), 0)
            FROM HoursLog h
            WHERE h.registration.volunteer.id = :volunteerId
            """)
    Double sumHoursByVolunteerId(@Param("volunteerId") UUID volunteerId);
}
