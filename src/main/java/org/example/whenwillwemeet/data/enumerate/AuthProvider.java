package org.example.whenwillwemeet.data.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthProvider {
  KAKAO("KAKAO"),
  NATIVE("NATIVE");

  private final String providerText;
}
