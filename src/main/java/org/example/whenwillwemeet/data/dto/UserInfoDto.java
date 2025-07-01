package org.example.whenwillwemeet.data.dto;

import lombok.Builder;
import org.example.whenwillwemeet.data.model.UserModel;
import org.example.whenwillwemeet.domain.entity.User;

@Builder
public record UserInfoDto(
    String userId,
    String name,
    String email
) {
  public static UserInfoDto of(User user) {
    return UserInfoDto.builder()
        .userId(user.getId().toString())
        .name(user.getName())
        .email(user.getEmail())
        .build();
  }
}
