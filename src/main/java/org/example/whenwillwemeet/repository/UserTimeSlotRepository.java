package org.example.whenwillwemeet.repository;

import org.example.whenwillwemeet.domain.entity.UserTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTimeSlotRepository extends JpaRepository<UserTimeSlot, Long> {
}
