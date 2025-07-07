package org.example.whenwillwemeet.converter;

import java.util.List;
import org.example.whenwillwemeet.data.dto.AppointmentCreateDto.ScheduleDto;
import org.example.whenwillwemeet.data.dto.AppointmentGetDto.ScheduleGetDto;
import org.example.whenwillwemeet.data.dto.AppointmentGetDto.TimeSlotGetDto;
import org.example.whenwillwemeet.domain.entity.Appointment;
import org.example.whenwillwemeet.domain.entity.Schedule;

public class ScheduleConverter {

  public static Schedule toEntity(ScheduleDto scheduleDto, Appointment appointment) {
    return Schedule.builder()
        .appointment(appointment)
        .date(scheduleDto.getDate())
        .build();
  }

  public static ScheduleGetDto toScheduleGetDto(Schedule schedule) {
    ScheduleGetDto build = ScheduleGetDto.builder()
        .id(schedule.getId())
        .date(schedule.getDate())
        .build();

    List<TimeSlotGetDto> times = build.getTimes();
    schedule.getTimeSlots().forEach(timeSlot -> {
      times.add(TimeSlotConverter.toTimeSlotGetResponse(timeSlot));
    });

    return build;
  }
}
