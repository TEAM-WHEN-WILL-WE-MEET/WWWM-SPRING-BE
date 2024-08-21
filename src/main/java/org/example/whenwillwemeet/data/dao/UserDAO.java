package org.example.whenwillwemeet.data.dao;

import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.example.whenwillwemeet.data.model.User;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Component
public class UserDAO {
    private final MongoTemplate mongoTemplate;

    public UserDAO(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public boolean updateUserInAppointment(String appointmentId,  User user) {
        try {
            Query query = new Query(Criteria.where("_id").is(appointmentId)
                    .and("users._id").is(user.getName()));

            Update update = new Update()
                    .set("users.$[usr].email", user.getEmail())
                    .set("users.$[usr].phoneNumber", user.getPhoneNumber());

            update.filterArray(Criteria.where("usr._id").is(user.getName()));

            UpdateResult result = mongoTemplate.updateFirst(query, update, "appointments");

            log.info("[UserDAO]-[updateUserInAppointment] Update result: {}", result);

            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            log.error("Error updating user in appointment: ", e);
            return false;
        }
    }
}