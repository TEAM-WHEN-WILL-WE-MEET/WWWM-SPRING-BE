package org.example.whenwillwemeet.data.dto;

import lombok.Builder;
import org.example.whenwillwemeet.data.model.UserModel;

@Builder
public record UserInfoDto(
    String userId,
    String name,
    String email
) {
  public static UserInfoDto of(UserModel userModel) {
    return UserInfoDto.builder()
        .userId(userModel.getId().toString())
        .name(userModel.getName())
        .email(userModel.getEmail())
        .build();
  }
}
