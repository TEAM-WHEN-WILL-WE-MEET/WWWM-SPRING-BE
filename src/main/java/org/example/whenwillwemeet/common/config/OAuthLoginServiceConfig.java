package org.example.whenwillwemeet.common.config;

import java.util.Map;
import org.example.whenwillwemeet.data.enumerate.AuthProvider;
import org.example.whenwillwemeet.service.KakaoOAuthService;
import org.example.whenwillwemeet.service.OAuthLoginService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuthLoginServiceConfig {

  @Bean
  public Map<AuthProvider, OAuthLoginService> loginServices(KakaoOAuthService kakao) {
    return Map.of(AuthProvider.KAKAO, kakao);
  }
}
