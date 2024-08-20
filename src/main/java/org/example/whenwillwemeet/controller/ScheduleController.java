package org.example.whenwillwemeet.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.data.model.Schedule;
import org.example.whenwillwemeet.data.model.User;
import org.example.whenwillwemeet.data.model.validation.ScheduleValidation;
import org.example.whenwillwemeet.data.model.validation.UserValidation;
import org.example.whenwillwemeet.service.ScheduleService;
import org.example.whenwillwemeet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/schedule")
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    ScheduleValidation scheduleValidation;

    @GetMapping(value="/getSchedule")
    public ResponseEntity<CommonResponse> getSchedule(@RequestParam("appointmentId") String appointmentId){
        log.info("[ScheduleController]-[getSchedule] API Called");

        if (appointmentId.isEmpty()) {
            log.warn("[AppointmentController]-[getAppointment] AppointmentId needed");
            CommonResponse response = new CommonResponse(false, HttpStatus.BAD_REQUEST, "AppointmentId needed");
        }

        CommonResponse response = scheduleService.getSchedule(appointmentId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping(value="/updateSchedule")
    public ResponseEntity<CommonResponse> login(@Valid @RequestBody Schedule schedule){
        log.info("[ScheduleController]-[updateSchedule] API Called");

        List<String> validationErrors = scheduleValidation.validateSchedule(schedule);

        if (!validationErrors.isEmpty()) {
            log.warn("[ScheduleController]-[updateSchedule] Validation failed: {}", validationErrors);
            CommonResponse response = new CommonResponse(false, HttpStatus.BAD_REQUEST, String.join(", ", validationErrors));
            return ResponseEntity.status(response.getStatus()).body(response);
        }

        CommonResponse response = scheduleService.updateSchedule(schedule, schedule.getTimes().getFirst().getUsers().getFirst());
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
