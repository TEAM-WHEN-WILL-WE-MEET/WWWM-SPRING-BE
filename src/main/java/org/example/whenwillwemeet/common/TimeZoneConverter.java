package org.example.whenwillwemeet.common;

import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.example.whenwillwemeet.data.model.Schedule;
import org.example.whenwillwemeet.data.model.TimeSlot;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

// MongoDB의 Date에서 Appointment에 저장된 TimeZone으로 변환하는 Class
@Slf4j
public class TimeZoneConverter {
    public static AppointmentModel convertToUTC(AppointmentModel appointment) {
        // UTC 기준으로 ZoneId 설정
        ZoneId targetZoneId = ZoneId.of("UTC");

        // createdAt, expireAt 변환
        if (appointment.getCreatedAt() != null) {
            appointment.setCreatedAt(convertToTimeZone(appointment.getCreatedAt(), targetZoneId));
        }
        if (appointment.getExpireAt() != null) {
            appointment.setExpireAt(convertToTimeZone(appointment.getExpireAt(), targetZoneId));
        }

        // startTime, endTime 변환
        if (appointment.getStartTime() != null) {
            appointment.setStartTime(convertToTimeZone(appointment.getStartTime(), targetZoneId));
        }
        if (appointment.getEndTime() != null) {
            appointment.setEndTime(convertToTimeZone(appointment.getEndTime(), targetZoneId));
        }

        // Schedules, TimeSlots 변환
        if (appointment.getSchedules() != null) {
            for (Schedule schedule : appointment.getSchedules()) {
                if (schedule.getDate() != null) {
                    schedule.setDate(convertToTimeZone(schedule.getDate(), targetZoneId));
                }
                if (schedule.getTimes() != null) {
                    for (TimeSlot timeSlot : schedule.getTimes()) {
                        if (timeSlot.getTime() != null) {
                            timeSlot.setTime(convertToTimeZone(timeSlot.getTime(), targetZoneId));
                        }
                    }
                }
            }
        }

        return appointment;
    }

    private static LocalDateTime convertToTimeZone(LocalDateTime localDateTime, ZoneId targetZoneId) {
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return zonedDateTime.withZoneSameInstant(targetZoneId).toLocalDateTime();
    }
}