package org.example.whenwillwemeet.data.dao;

import com.mongodb.client.result.UpdateResult;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.example.whenwillwemeet.common.exception.ApplicationException;
import org.example.whenwillwemeet.common.exception.ErrorCode;
import org.example.whenwillwemeet.data.model.User;
import org.example.whenwillwemeet.data.model.UserModel;
import org.example.whenwillwemeet.data.repository.UserRepository;
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
@RequiredArgsConstructor
public class UserDAO {

  private final MongoTemplate mongoTemplate;
  private final UserRepository userRepository;

  public void save(UserModel userModel) {
    userRepository.save(userModel);
  }

  public Optional<UserModel> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public boolean updateUserInAppointment(String appointmentId, User user) {
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