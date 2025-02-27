package org.example.whenwillwemeet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.data.dto.UserLoginDto;
import org.example.whenwillwemeet.data.dto.UserSignupDto;
import org.example.whenwillwemeet.service.UserAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/user")
public class UserControllerV2 {

  private final UserAuthService userAuthService;

  @PostMapping("/login")
  public ResponseEntity<CommonResponse> login(@Valid @RequestBody UserLoginDto userLoginDto) {
    CommonResponse response = userAuthService.login(userLoginDto);
    return ResponseEntity.status(response.getStatus()).body(response);
  }

  @PostMapping("/signup")
  public ResponseEntity<CommonResponse> signup(@Valid @RequestBody UserSignupDto userSignupDto) {
    CommonResponse respone = userAuthService.signup(userSignupDto);
    return ResponseEntity.status(respone.getStatus()).body(respone);
  }

}
