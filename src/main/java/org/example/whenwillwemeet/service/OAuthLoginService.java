package org.example.whenwillwemeet.service;

import org.example.whenwillwemeet.common.CommonResponse;

public interface OAuthLoginService {
  CommonResponse getLoginUrl();
  CommonResponse loginWithCode(String code);
}
