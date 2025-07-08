package org.example.whenwillwemeet.data.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.whenwillwemeet.common.aop.annotation.ValidZoneId;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreateDto {

  @NotEmpty(message = "Name is required")
  private String name;

  @NotEmpty(message = "At least one schedule is required")
  @Valid
  private List<ScheduleDto> schedules = new ArrayList<>();

  @NotNull(message = "Start time is required")
  private LocalTime startTime;

  @NotNull(message = "End time is required")
  private LocalTime endTime;

  @NotEmpty(message = "Time zone is required")
  @ValidZoneId
  private String timeZone;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ScheduleDto {

    @NotNull(message = "Schedule date is required")
    private LocalDate date;
  }
}
