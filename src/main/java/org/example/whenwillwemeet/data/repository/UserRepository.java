package org.example.whenwillwemeet.data.repository;

import java.util.Optional;
import java.util.UUID;
import org.example.whenwillwemeet.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}
