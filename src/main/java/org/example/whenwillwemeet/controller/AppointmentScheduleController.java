package org.example.whenwillwemeet.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.aop.annotation.LoginUserId;
import org.example.whenwillwemeet.data.dto.ScheduleUpdateDto;
import org.example.whenwillwemeet.data.dto.TimeslotToggleDto;
import org.example.whenwillwemeet.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/appointments/{appointmentId}/schedules")
public class AppointmentScheduleController {

  private final ScheduleService scheduleService;

  @GetMapping("")
  public ResponseEntity<CommonResponse> getScheduleByAppointment(@PathVariable UUID appointmentId) {
    CommonResponse response = scheduleService.getSchedule(appointmentId);
    return ResponseEntity.status(response.getStatus()).body(response);
  }

  @Deprecated
  @PatchMapping("")
  public ResponseEntity<CommonResponse> updateUserTimeSlots(
      @PathVariable UUID appointmentId,
      @Valid @RequestBody ScheduleUpdateDto schedule,
      @LoginUserId UUID loginUserId
  ) {
    CommonResponse response = scheduleService.updateUserTimeSlots(appointmentId, schedule,
        loginUserId);
    return ResponseEntity.status(response.getStatus()).body(response);
  }

  @PatchMapping("/{scheduleId}/timeslots/toggle")
  public ResponseEntity<CommonResponse> updateTimeSlots(
      @PathVariable UUID appointmentId,
      @PathVariable Long scheduleId,
      @Valid @RequestBody TimeslotToggleDto dto,
      @LoginUserId UUID loginUserId
  ) {
    CommonResponse response;
    if (dto.isEnabled()) {
      response = scheduleService.enableTimeslot(appointmentId, scheduleId, dto, loginUserId);
    }
    else {
      response = scheduleService.disabledTimeslot(appointmentId, scheduleId, dto, loginUserId);
    }

    return ResponseEntity.status(response.getStatus()).body(response);
  }

  @GetMapping(value = "/users/{userId}")
  public ResponseEntity<CommonResponse> getUserSchedule(
      @PathVariable UUID appointmentId,
      @PathVariable UUID userId
  ) {
    CommonResponse response = scheduleService.getUserSchedule(appointmentId, userId);
    return ResponseEntity.status(response.getStatus()).body(response);
  }
}
