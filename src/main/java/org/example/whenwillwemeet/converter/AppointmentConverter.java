package org.example.whenwillwemeet.converter;

import java.util.List;
import org.example.whenwillwemeet.data.dto.AppointmentCreateDto;
import org.example.whenwillwemeet.data.dto.AppointmentGetDto;
import org.example.whenwillwemeet.data.dto.AppointmentGetDto.ScheduleGetDto;
import org.example.whenwillwemeet.data.dto.AppointmentGetDto.UserGetDto;
import org.example.whenwillwemeet.data.dto.MyAppointmentGetDto;
import org.example.whenwillwemeet.domain.entity.Appointment;

public class AppointmentConverter {
  public static Appointment toEntity(AppointmentCreateDto appointmentCreateDto) {
    return Appointment.builder()
        .name(appointmentCreateDto.getName())
        .startTime(appointmentCreateDto.getStartTime())
        .endTime(appointmentCreateDto.getEndTime())
        .timeZone(appointmentCreateDto.getTimeZone())
        .build();
  }

  public static AppointmentGetDto toResponseDto(Appointment appointment) {
    AppointmentGetDto build = AppointmentGetDto.builder()
        .id(appointment.getId())
        .createdAt(appointment.getCreatedAt())
        .expireAt(appointment.getExpireAt())
        .startTime(appointment.getStartTime())
        .endTime(appointment.getEndTime())
        .timeZone(appointment.getTimeZone())
        .name(appointment.getName())
        .build();

    List<ScheduleGetDto> schedules = build.getSchedules();
    appointment.getSchedules().forEach(schedule -> {
      schedules.add(ScheduleConverter.toScheduleGetDto(schedule));
    });

    List<UserGetDto> users = build.getUsers();
    appointment.getUsers().forEach(user -> {
      users.add(UserConverter.toUserGetDto(user.getUser()));
    });

    return build;
  }

  public static MyAppointmentGetDto toMyAppointmentGetDto(Appointment appointment) {
    return MyAppointmentGetDto.builder()
        .id(appointment.getId())
        .createdAt(appointment.getCreatedAt())
        .expireAt(appointment.getExpireAt())
        .startTime(appointment.getStartTime())
        .endTime(appointment.getEndTime())
        .timeZone(appointment.getTimeZone())
        .name(appointment.getName())
        .build();
  }
}
