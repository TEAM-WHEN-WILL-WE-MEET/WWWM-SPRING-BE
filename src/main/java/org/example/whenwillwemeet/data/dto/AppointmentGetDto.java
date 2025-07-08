package org.example.whenwillwemeet.data.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentGetDto {
  private UUID id;
  private LocalDateTime createdAt;
  private LocalDateTime expireAt;
  private String name;
  private LocalTime startTime;
  private LocalTime endTime;
  private String timeZone;
  @Builder.Default
  private List<ScheduleGetDto> schedules = new ArrayList<>();
  @Builder.Default
  private List<UserGetDto> users = new ArrayList<>();

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ScheduleGetDto {
    private Long id;
    private LocalDate date;
    @Builder.Default
    private List<TimeSlotGetDto> times = new ArrayList<>();
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TimeSlotGetDto {
    private LocalDateTime time;
    @Builder.Default
    private List<UUID> users = new ArrayList<>();
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserGetDto {
    private UUID id;
    private String name;
    private String email;
  }
}
