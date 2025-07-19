package org.example.whenwillwemeet.data.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeslotToggleDto {

  @NotNull(message = "Enabled is required")
  private boolean enabled;

  @NotEmpty(message = "At least one time must be provided")
  private List<@NotNull(message = "Time value cannot be null") LocalDateTime> times = new ArrayList<>();
}
