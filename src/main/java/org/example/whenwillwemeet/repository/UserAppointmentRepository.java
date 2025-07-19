package org.example.whenwillwemeet.repository;

import java.util.List;
import java.util.Optional;
import org.example.whenwillwemeet.domain.entity.Appointment;
import org.example.whenwillwemeet.domain.entity.User;
import org.example.whenwillwemeet.domain.entity.UserAppointment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAppointmentRepository extends JpaRepository<UserAppointment, Long> {

  boolean existsByUserAndAppointment(User user, Appointment appointment);

  @EntityGraph(attributePaths = {"appointment"})
  List<UserAppointment> findByUser(User user);

  void deleteByAppointmentAndUser(Appointment appointmentReference, User me);

  int countByAppointment(Appointment appointmentRef);
}
