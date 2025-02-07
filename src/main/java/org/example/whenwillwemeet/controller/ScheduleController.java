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
        if (appointmentId.isEmpty()) {
            log.warn("[ScheduleController]-[getSchedule] AppointmentId needed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(false, HttpStatus.BAD_REQUEST, "AppointmentId needed"));
        }

        CommonResponse response = scheduleService.getSchedule(appointmentId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping(value="/updateSchedule")
    public ResponseEntity<CommonResponse> login(@Valid @RequestBody Schedule schedule){
        List<String> validationErrors = scheduleValidation.validateSchedule(schedule);

        if (!validationErrors.isEmpty()) {
            log.warn("[ScheduleController]-[updateSchedule] Validation failed: {}", validationErrors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(false, HttpStatus.BAD_REQUEST, String.join(", ", validationErrors)));
        }

        CommonResponse response = scheduleService.updateSchedule(schedule);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping(value="/getUserSchedule")
    public ResponseEntity<CommonResponse> getUserSchedule(@RequestParam("appointmentId") String appointmentId, @RequestParam("userName") String userName){
        if (appointmentId.isEmpty() || userName.isEmpty()) {
            log.warn("[ScheduleController]-[getUserSchedule] AppointmentId, UserName needed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(false, HttpStatus.BAD_REQUEST, "AppointmentId, UserName needed"));
        }

        CommonResponse response = scheduleService.getUserSchedule(appointmentId, userName);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
