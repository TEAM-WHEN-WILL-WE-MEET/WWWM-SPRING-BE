package org.example.whenwillwemeet.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.environment.ConfigUtil;
import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.example.whenwillwemeet.data.model.Schedule;
import org.example.whenwillwemeet.data.model.validation.AppointmentValidation;
import org.example.whenwillwemeet.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/appointment")
public class AppointmentController {
    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    AppointmentValidation appointmentValidation;

    @Autowired
    private ConfigUtil configUtil;

    private CommonResponse response;

    @PostMapping(value="/createAppointment")
    public ResponseEntity<CommonResponse> createAppointment(@Valid @RequestBody AppointmentModel appointmentModel){
        log.info("[AppointmentController]-[createAppointment] API Called");

        List<String> validationErrors = appointmentValidation.validateAppointmentModel(appointmentModel);

        if (!validationErrors.isEmpty()) {
            log.warn("[AppointmentController]-[createAppointment] Validation failed: {}", validationErrors);
            CommonResponse response = new CommonResponse(false, HttpStatus.BAD_REQUEST, String.join(", ", validationErrors));
            return ResponseEntity.status(response.getStatus()).body(response);
        }

        CommonResponse response = appointmentService.createAppointment(appointmentModel);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
