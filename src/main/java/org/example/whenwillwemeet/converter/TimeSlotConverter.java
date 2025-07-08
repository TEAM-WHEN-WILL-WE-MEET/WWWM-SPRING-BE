package org.example.whenwillwemeet.converter;

import java.util.List;
import java.util.UUID;
import org.example.whenwillwemeet.data.dto.AppointmentGetDto.TimeSlotGetDto;
import org.example.whenwillwemeet.domain.entity.TimeSlot;

public class TimeSlotConverter {

  public static TimeSlotGetDto toTimeSlotGetResponse(TimeSlot timeSlot) {
    TimeSlotGetDto build = TimeSlotGetDto.builder()
        .time(timeSlot.getTime())
        .build();

    List<UUID> userIds = build.getUsers();
    timeSlot.getUsers().forEach(user -> {
      userIds.add(user.getUser().getId());
    });

    return build;
  }
}
