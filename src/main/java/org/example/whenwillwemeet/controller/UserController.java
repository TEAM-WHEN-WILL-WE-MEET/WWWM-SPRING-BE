package org.example.whenwillwemeet.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.environment.ConfigUtil;
import org.example.whenwillwemeet.data.model.User;
import org.example.whenwillwemeet.data.model.validation.AppointmentValidation;
import org.example.whenwillwemeet.data.model.validation.UserValidation;
import org.example.whenwillwemeet.service.AppointmentService;
import org.example.whenwillwemeet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    UserValidation userValidation;

    @PostMapping(value="/login")
    public ResponseEntity<CommonResponse> login(@Valid @RequestBody User user){
        log.info("[UserController]-[login] API Called");

        List<String> validationErrors = userValidation.validateUser(user);

        if (!validationErrors.isEmpty()) {
            log.warn("[UserController]-[login] Validation failed: {}", validationErrors);
            CommonResponse response = new CommonResponse(false, HttpStatus.BAD_REQUEST, String.join(", ", validationErrors));
            return ResponseEntity.status(response.getStatus()).body(response);
        }

        CommonResponse response = userService.login(user);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping(value="/updateUser")
    public ResponseEntity<CommonResponse> updateUser(@Valid @RequestBody User user){
        log.info("[UserController]-[updateUser] API Called");

        List<String> validationErrors = userValidation.validateUser(user);

        if (!validationErrors.isEmpty()) {
            log.warn("[UserController]-[updateUser] Validation failed: {}", validationErrors);
            CommonResponse response = new CommonResponse(false, HttpStatus.BAD_REQUEST, String.join(", ", validationErrors));
            return ResponseEntity.status(response.getStatus()).body(response);
        }

        CommonResponse response = userService.updateUser(user);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
