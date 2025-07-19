package org.example.whenwillwemeet.repository;


import java.util.Collection;
import java.util.List;
import org.example.whenwillwemeet.domain.entity.TimeSlot;
import org.example.whenwillwemeet.domain.entity.User;
import org.example.whenwillwemeet.domain.entity.UserTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTimeSlotRepository extends JpaRepository<UserTimeSlot, Long> {

  List<UserTimeSlot> findByUserAndTimeSlotIn(User user, Collection<TimeSlot> timeSlots);
}
