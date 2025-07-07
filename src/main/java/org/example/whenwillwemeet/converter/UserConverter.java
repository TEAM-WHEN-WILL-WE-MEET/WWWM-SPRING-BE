package org.example.whenwillwemeet.converter;

import org.example.whenwillwemeet.data.dto.AppointmentGetDto.UserGetDto;
import org.example.whenwillwemeet.domain.entity.User;

public class UserConverter {

  public static UserGetDto toUserGetDto(User user) {
    return UserGetDto.builder()
        .id(user.getId())
        .name(user.getName())
        .email(user.getEmail())
        .build();
  }
}
