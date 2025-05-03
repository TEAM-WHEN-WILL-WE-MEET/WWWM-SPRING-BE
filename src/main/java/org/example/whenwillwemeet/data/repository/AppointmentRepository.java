package org.example.whenwillwemeet.data.repository;

import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends MongoRepository<AppointmentModel, String> {

}