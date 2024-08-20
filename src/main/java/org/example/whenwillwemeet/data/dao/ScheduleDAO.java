package org.example.whenwillwemeet.data.dao;


import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.example.whenwillwemeet.data.model.TimeSlot;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Component
public class ScheduleDAO {
    private final MongoTemplate mongoTemplate;

    public ScheduleDAO(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void addUserToTimeSlot(String appointmentId, String scheduleId, LocalDateTime time, String userName, String zoneId) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(appointmentId))
                .and("schedules._id").is(scheduleId)
                .and("schedules.times.time").is(Date.from(time.atZone(ZoneId.of(zoneId)).toInstant())));

        Update update = new Update().addToSet("schedules.$[sched].times.$[slot].users", userName);

        update.filterArray(Criteria.where("sched._id").is(scheduleId));
        update.filterArray(Criteria.where("slot.time").is(Date.from(time.atZone(ZoneId.of(zoneId)).toInstant())));

        mongoTemplate.updateFirst(query, update, "appointments");
    }

    public void removeUserFromTimeSlot(String appointmentId, String scheduleId, LocalDateTime time, String userName, String zoneId) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(appointmentId))
                .and("schedules._id").is(scheduleId)
                .and("schedules.times.time").is(Date.from(time.atZone(ZoneId.of(zoneId)).toInstant())));

        Update update = new Update().pull("schedules.$[sched].times.$[slot].users", userName);

        update.filterArray(Criteria.where("sched._id").is(scheduleId));
        update.filterArray(Criteria.where("slot.time").is(Date.from(time.atZone(ZoneId.of(zoneId)).toInstant())));

        mongoTemplate.updateFirst(query, update, "appointments");
    }
}
