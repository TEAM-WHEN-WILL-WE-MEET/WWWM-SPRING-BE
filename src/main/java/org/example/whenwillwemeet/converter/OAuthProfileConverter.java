package org.example.whenwillwemeet.converter;

import org.example.whenwillwemeet.data.dto.KakaoProfileResponse;
import org.example.whenwillwemeet.data.dto.UserLoginDto;
import org.example.whenwillwemeet.data.dto.UserSignupDto;

public class OAuthProfileConverter {

  public static UserLoginDto profileToLoginReq(KakaoProfileResponse profile) {
    return UserLoginDto.builder()
        .email(profile.getKakao_account().getEmail())
        .password(profile.getKakao_account().getEmail())
        .build();
  }

  public static UserSignupDto profileToSignupReq(KakaoProfileResponse profile) {
    return UserSignupDto.builder()
        .email(profile.getKakao_account().getEmail())
        .password(profile.getKakao_account().getEmail())
        .name(profile.getKakao_account().getName())
        //.provider(AuthProvider.KAKAO)
        .build();
  }
}
