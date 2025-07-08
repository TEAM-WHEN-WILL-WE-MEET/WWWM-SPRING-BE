package org.example.whenwillwemeet.common;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.example.whenwillwemeet.domain.entity.Appointment;

// MongoDB의 Date에서 Appointment에 저장된 TimeZone으로 변환하는 Class
@Slf4j
public class TimeZoneConverter {
/*
  public static Appointment convertToUTC(Appointment appointment) {
    // UTC 기준으로 ZoneId 설정
    ZoneId targetZoneId = ZoneId.of("UTC");

    appointment.updateTimes(
        convertToTimeZone(appointment.getStartTime(), targetZoneId),
        convertToTimeZone(appointment.getEndTime(), targetZoneId),
        convertToTimeZone(appointment.getExpireAt(), targetZoneId),
        convertToTimeZone(appointment.getCreatedAt(), targetZoneId)
    );

    // Schedules, TimeSlots 변환
    appointment.getSchedules().forEach(schedule -> {
      schedule.updateDate(convertToTimeZone(schedule.getDate(), targetZoneId));
      schedule.getTimeSlots().forEach(
          timeSlot -> timeSlot.updateTime(convertToTimeZone(timeSlot.getTime(), targetZoneId)));
    });

    return appointment;
  }

  private static LocalDateTime convertToTimeZone(LocalDateTime localDateTime, ZoneId targetZoneId) {
    if (localDateTime == null) {
      return null;
    }
    ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
    return zonedDateTime.withZoneSameInstant(targetZoneId).toLocalDateTime();
  }

 */
}