package org.example.whenwillwemeet.data.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KakaoProfileResponse {

  private Long id;
  private String connected_at;
  private KakaoAccount kakao_account;

  @Data
  @NoArgsConstructor
  public static class KakaoAccount {
    private String email;
    private String name;
  }
}
