package org.example.whenwillwemeet.data.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.constant.ConstantVariables;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "appointments")
public class AppointmentModel {
    @Id
    private String id;

    @CreatedDate
    @Indexed(direction = IndexDirection.DESCENDING)
    private LocalDateTime createdAt;
    @Indexed(expireAfterSeconds = 0)
    private LocalDateTime expireAt;

    private String name;

    // @DBRef
    private List<Schedule> schedules;
    // @DBRef
    private List<User> users;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String timeZone;

    // APPOINTMENT_EXPIRATION_TIME static 상수를 통해 ExpireAt 설정
    // TTL 기능을 활성화하기 위해 별도로 MongoDB Collectiond에 TTL Index 추가
    public void initializeTimes() {
        ZoneId appointmentZoneId = ZoneId.of(this.timeZone);
        ZonedDateTime nowInAppointmentZone = ZonedDateTime.now(appointmentZoneId);
        log.info("[AppointmentModel]-[initializeTimes] Now In Appointment TimeZone {}", nowInAppointmentZone);

        this.createdAt = nowInAppointmentZone.toLocalDateTime();
        this.expireAt = nowInAppointmentZone.plusDays(ConstantVariables.APPOINTMENT_EXPIRATION_TIME).toLocalDateTime();
    }
}