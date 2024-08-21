package org.example.whenwillwemeet.data.repository;

import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.example.whenwillwemeet.data.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends MongoRepository<AppointmentModel, String> {
    @Query("{ 'id': ?0 }")
    @Update("{ $addToSet: { 'users': ?1 } }")
    void addUser(String appointmentId, User user);

    @Query("{ 'id': ?0 , 'users.name': ?1}")
    @Update("{ $set: { 'users': ?2 } }")
    void updateUser(String appointmentId, String name, User user);
}