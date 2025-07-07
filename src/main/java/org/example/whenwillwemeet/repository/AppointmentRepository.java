package org.example.whenwillwemeet.repository;

import java.util.Optional;
import java.util.UUID;
import org.example.whenwillwemeet.domain.entity.Appointment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

  @EntityGraph(attributePaths = {"schedules"})
  Optional<Appointment> findWithSchedulesById(UUID id);
}