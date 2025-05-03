package org.example.whenwillwemeet.data.dao;


import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import org.bson.types.ObjectId;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.example.whenwillwemeet.data.model.Schedule;
import org.example.whenwillwemeet.data.model.TimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.bson.Document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Component
public class ScheduleDAO {
    @Autowired
    AppointmentDAO appointmentDAO;

    private final MongoTemplate mongoTemplate;

    public ScheduleDAO(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // User가 약속 일정 내에 존재하는지 여부를 검사하는 메소드
    public boolean isUserExistsInAppointment(String appointmentId, String userName) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(appointmentId))
                .and("users._id").is(userName));

        // exists를 통해 존재 여부만 확인
        boolean exists = mongoTemplate.exists(query, "appointments");
        log.warn("User exists: {}", exists);

        return exists;
    }

    // MongoDB Driver의 Document를 직접 활용
    public void updateUserInTimeSlotsBulk(String appointmentId, String scheduleId, List<LocalDateTime> timesToAdd, List<LocalDateTime> timesToRemove, String userName, String zoneId) {
        List<WriteModel<Document>> bulkOperations = new ArrayList<>();

        for (LocalDateTime time : timesToAdd) {
            Document query = new Document("_id", new ObjectId(appointmentId))
                    .append("schedules._id", scheduleId)
                    .append("schedules.times.time", Date.from(time.atZone(ZoneId.of(zoneId)).toInstant()));

            Document update = new Document("$addToSet",
                    new Document("schedules.$[sched].times.$[slot].users", userName));

            UpdateOptions updateOptions = new UpdateOptions().arrayFilters(
                    Arrays.asList(
                            new Document("sched._id", scheduleId),
                            new Document("slot.time", Date.from(time.atZone(ZoneId.of(zoneId)).toInstant()))
                    )
            );

            bulkOperations.add(new UpdateOneModel<>(query, update, updateOptions));
        }

        for (LocalDateTime time : timesToRemove) {
            Document query = new Document("_id", new ObjectId(appointmentId))
                    .append("schedules._id", scheduleId)
                    .append("schedules.times.time", Date.from(time.atZone(ZoneId.of(zoneId)).toInstant()));

            Document update = new Document("$pull",
                    new Document("schedules.$[sched].times.$[slot].users", userName));

            UpdateOptions updateOptions = new UpdateOptions().arrayFilters(
                    Arrays.asList(
                            new Document("sched._id", scheduleId),
                            new Document("slot.time", Date.from(time.atZone(ZoneId.of(zoneId)).toInstant()))
                    )
            );

            bulkOperations.add(new UpdateOneModel<>(query, update, updateOptions));
        }

        if (!bulkOperations.isEmpty()) {
            mongoTemplate.getCollection("appointments").bulkWrite(bulkOperations);
        }
    }

    public CommonResponse getUserSchedule(String appointmentId, String userName) {
        try{
            Optional<AppointmentModel> appointmentModel = appointmentDAO.getAppointmentModelById(appointmentId);
            if(!isUserExistsInAppointment(appointmentId, userName)) {
                throw new RuntimeException("User [" + userName + "] not found in " + appointmentId);
            } else if(appointmentModel.isPresent()) {
                List<Schedule> userSchedule = new ArrayList<>();

                for(Schedule i : appointmentModel.get().getSchedules()){
                    Schedule tmpSchedule = new Schedule();
                    tmpSchedule.setId(i.getId());
                    tmpSchedule.setDate(i.getDate());

                    List<TimeSlot> tmpTimeSlot = new ArrayList<>();
                    for(TimeSlot j : i.getTimes()){
                        if(j.getUsers().contains(userName))
                            tmpTimeSlot.add(j);
                    }

                    if(!tmpTimeSlot.isEmpty()) {
                        tmpSchedule.setTimes(tmpTimeSlot);
                        userSchedule.add(tmpSchedule);
                    }
                }

                return new CommonResponse(true, HttpStatus.OK, "User [" + userName + "] schedule fetched", userSchedule);
            }
            else
                throw new RuntimeException("Appointment not found with id: " + appointmentId);
        }catch (Exception e){
            return new CommonResponse(false, HttpStatus.INTERNAL_SERVER_ERROR, "Appointment fetch failed with : [" + e + "]");
        }
    }
}
