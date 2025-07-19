package org.example.whenwillwemeet.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.example.whenwillwemeet.domain.entity.Schedule;
import org.example.whenwillwemeet.domain.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

  List<TimeSlot> findByScheduleAndTimeIn(Schedule schedule, Collection<LocalDateTime> times);
}
