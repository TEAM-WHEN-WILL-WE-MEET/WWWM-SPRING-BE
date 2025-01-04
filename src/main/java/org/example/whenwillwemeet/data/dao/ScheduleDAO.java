package org.example.whenwillwemeet.data.dao;


import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.example.whenwillwemeet.data.model.Schedule;
import org.example.whenwillwemeet.data.model.TimeSlot;
import org.example.whenwillwemeet.data.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
