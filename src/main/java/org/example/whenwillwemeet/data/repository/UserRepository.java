package org.example.whenwillwemeet.data.repository;

import java.util.Optional;
import org.bson.types.ObjectId;
import org.example.whenwillwemeet.data.model.UserModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<UserModel, ObjectId> {
    Optional<UserModel> findByEmail(String email);
}
