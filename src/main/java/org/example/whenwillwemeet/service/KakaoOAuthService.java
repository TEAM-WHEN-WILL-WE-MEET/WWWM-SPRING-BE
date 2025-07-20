package org.example.whenwillwemeet.service;


import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.common.constant.KakaoOAuthConstant;
import org.example.whenwillwemeet.common.exception.ApplicationException;
import org.example.whenwillwemeet.converter.OAuthProfileConverter;
import org.example.whenwillwemeet.data.dto.KakaoProfileResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService implements OAuthLoginService {

  private final KakaoOAuthConstant constant;
  private final RestTemplate restTemplate = new RestTemplate();
  private final UserAuthService userAuthService;

  @Override
  public CommonResponse getLoginUrl() {
    String loginUrl = UriComponentsBuilder.fromHttpUrl("https://kauth.kakao.com/oauth/authorize")
        .queryParam("client_id", constant.getClientId())
        .queryParam("redirect_uri", constant.getRedirectUri())
        .queryParam("response_type", "code")
        .queryParam("scope", constant.getScope())
        .build().toUriString();

    return new CommonResponse(true, HttpStatus.OK, "요청에 성공했습니다.", loginUrl);
  }

  @Override
  public CommonResponse loginWithCode(String code) {
    String accessToken = getAccessToken(code);
    KakaoProfileResponse userInfo = getUserInfo(accessToken);
    CommonResponse response;

    try {
      response = userAuthService.login(OAuthProfileConverter.profileToLoginReq(userInfo));
    } catch (ApplicationException e) {
      userAuthService.signup(OAuthProfileConverter.profileToSignupReq(userInfo));
      response = userAuthService.login(OAuthProfileConverter.profileToLoginReq(userInfo));
    }

    return response;
  }

  private String getAccessToken(String code) {
    // 1. Access Token 요청
    HttpHeaders tokenHeaders = new HttpHeaders();
    tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
    tokenParams.add("grant_type", "authorization_code");
    tokenParams.add("client_id", constant.getClientId());
    tokenParams.add("redirect_uri", constant.getRedirectUri());
    tokenParams.add("code", code);

    HttpEntity<?> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);
    ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
        "https://kauth.kakao.com/oauth/token", tokenRequest, Map.class
    );

    if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
      throw new RuntimeException("카카오 access token 요청 실패");
    }

    return (String) Objects.requireNonNull(tokenResponse.getBody()).get("access_token");
  }

  private KakaoProfileResponse getUserInfo(String accessToken) {
    // 2. 사용자 정보 요청
    HttpHeaders profileHeaders = new HttpHeaders();
    profileHeaders.setBearerAuth(accessToken);

    HttpEntity<?> profileRequest = new HttpEntity<>(profileHeaders);
    ResponseEntity<KakaoProfileResponse> profileResponse = restTemplate.exchange(
        "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, profileRequest,
        KakaoProfileResponse.class
    );

    return profileResponse.getBody();
  }
}
