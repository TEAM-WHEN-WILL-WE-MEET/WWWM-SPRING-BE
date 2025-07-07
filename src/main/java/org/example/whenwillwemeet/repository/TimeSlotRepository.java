package org.example.whenwillwemeet.repository;

import org.example.whenwillwemeet.domain.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
}
