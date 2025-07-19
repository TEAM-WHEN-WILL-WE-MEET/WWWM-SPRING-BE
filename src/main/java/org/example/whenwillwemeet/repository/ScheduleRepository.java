package org.example.whenwillwemeet.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.whenwillwemeet.domain.entity.Appointment;
import org.example.whenwillwemeet.domain.entity.Schedule;
import org.example.whenwillwemeet.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

  @Query("""
        SELECT DISTINCT s FROM Schedule s
        JOIN FETCH s.timeSlots ts
        WHERE s.id = :scheduleId
          AND ts.time IN :times
      """)
  Optional<Schedule> findWithTimeSlotByIdAndTimesIn(
      @Param("scheduleId") Long scheduleId,
      @Param("times") List<LocalDateTime> times
  );

  @Query("""
        SELECT DISTINCT s FROM Schedule s
        JOIN FETCH s.timeSlots ts
        JOIN ts.users uts
        WHERE s.appointment.id = :appointmentId
          AND uts.user = :user
      """)
  List<Schedule> findWithTimeSlotByAppointmentIdAndUser(
      @Param("appointmentId") UUID appointmentId,
      @Param("user") User user);

  List<Schedule> findByAppointment(Appointment appointment);
}
