package org.example.whenwillwemeet.data.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "appointments")
public class AppointmentModel {
    @Id
    private String id;

    @CreatedDate
    @Indexed(direction = IndexDirection.DESCENDING)
    private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
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
}