package org.example.whenwillwemeet.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.data.enumerate.AuthProvider;
import org.example.whenwillwemeet.service.OAuthLoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/oauth2")
@RequiredArgsConstructor
public class OAuthController {

  private final Map<AuthProvider, OAuthLoginService> loginServices;

  @GetMapping("/{provider}/login")
  public ResponseEntity<CommonResponse> getLoginUrl(@PathVariable AuthProvider provider) {
    CommonResponse response = loginServices.get(provider).getLoginUrl();
    return ResponseEntity.ok(response);
  }

  @RequestMapping(value = "/{provider}/callback", method = {RequestMethod.GET, RequestMethod.POST})
  public ResponseEntity<CommonResponse> loginCallback(@PathVariable AuthProvider provider,
      @RequestParam String code) {
    CommonResponse response = loginServices.get(provider).loginWithCode(code);
    return ResponseEntity.ok(response);
  }

}
