package org.example.whenwillwemeet.data.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyAppointmentGetDto {

  private UUID id;
  private LocalDateTime createdAt;
  private LocalDateTime expireAt;
  private String name;
  private LocalTime startTime;
  private LocalTime endTime;
  private String timeZone;
}
