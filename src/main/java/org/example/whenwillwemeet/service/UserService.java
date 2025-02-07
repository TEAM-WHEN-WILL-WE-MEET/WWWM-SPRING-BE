package org.example.whenwillwemeet.service;

import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.data.dao.AppointmentDAO;
import org.example.whenwillwemeet.data.dao.UserDAO;
import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.example.whenwillwemeet.data.model.User;
import org.example.whenwillwemeet.data.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
public class UserService {
    @Autowired
    private AppointmentDAO appointmentDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public CommonResponse login(User user) {
        Optional<AppointmentModel> appointmentModelOptional = appointmentDAO.getAppointmentModelById(user.getAppointmentId());

        if(appointmentModelOptional.isEmpty()) {
            return new CommonResponse(false, HttpStatus.NOT_FOUND, "Appointment not found");
        }

        AppointmentModel appointmentModel = appointmentModelOptional.get();

        if(appointmentModel.getUsers() == null)
            appointmentModel.setUsers(new ArrayList<>());

        User existingUser = findUserByName(appointmentModel, user.getName());

        if (existingUser != null) {
            // 사용자가 존재하는 경우
            if (passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                // 비밀번호가 일치하는 경우
                existingUser.setPassword(null);
                log.info("[UserService]-[login] User [{}] login succeeded [{}]", user.getName(), appointmentModel.getId());
                return new CommonResponse(true, HttpStatus.OK, "Login success", existingUser);
            } else {
                // 비밀번호가 불일치하는 경우
                log.info("[UserService]-[login] User [{}] login failed (Incorrect password) [{}]", user.getName(), appointmentModel.getId());
                return new CommonResponse(false, HttpStatus.UNAUTHORIZED, "Login failed: Incorrect password");
            }
        } else {
            // 사용자가 존재하지 않는 경우, 새로운 사용자 생성
            User newUser = new User();
            newUser.setName(user.getName());
            newUser.setPassword(passwordEncoder.encode(user.getPassword()));
            newUser.setEmail(user.getEmail());
            newUser.setPhoneNumber(user.getPhoneNumber());

            appointmentModel.getUsers().add(newUser);
            CommonResponse updateResponse = appointmentDAO.addUserToAppointment(appointmentModel.getId(), newUser);

            if (updateResponse.isSuccess()) {
                newUser.setPassword(null);
                log.info("[UserService]-[login] New User [{}] registered to [{}]", user.getName(), appointmentModel.getId());
                return new CommonResponse(true, HttpStatus.CREATED, "New user registered", newUser);
            } else {
                log.error("[UserService]-[login] New User [{}] registration failed to [{}]", user.getName(), appointmentModel.getId());
                return new CommonResponse(false, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to register new user");
            }
        }
    }

    public CommonResponse updateUser(User updatedUser) {
        Optional<AppointmentModel> appointmentModelOptional = appointmentDAO.getAppointmentModelById(updatedUser.getAppointmentId());

        if(appointmentModelOptional.isEmpty()) {
            return new CommonResponse(false, HttpStatus.NOT_FOUND, "Appointment not found");
        }

        AppointmentModel appointmentModel = appointmentModelOptional.get();
        User existingUser = findUserByName(appointmentModel, updatedUser.getName());

        // 기존 User 존재 여부 검증 및 비밀번호 검증
        if (existingUser == null) {
            log.error("[UserService]-[updateUser] User [{}] not found in [{}]", updatedUser.getName(), appointmentModel.getId());
            return new CommonResponse(false, HttpStatus.NOT_FOUND, "User not found");
        } else if (!passwordEncoder.matches(updatedUser.getPassword(), existingUser.getPassword())) {
            log.error("[UserService]-[updateUser] User [{}] password is incorrect [{}]", updatedUser.getName(), appointmentModel.getId());
            return new CommonResponse(false, HttpStatus.UNAUTHORIZED, "Incorrect password");
        }

        // 업데이트할 정보 설정
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());

        // User 업데이트
        if (userDAO.updateUserInAppointment(appointmentModel.getId(), existingUser)) {
            existingUser.setPassword(null);
            log.info("[UserService]-[updateUser] User [{}] information updated in [{}]", existingUser.getName(), appointmentModel.getId());
            return new CommonResponse(true, HttpStatus.OK, "User information updated successfully [" + existingUser.getName() + "]");
        } else {
            log.error("[UserService]-[updateUser] User [{}] information update failed in [{}]", existingUser.getName(), appointmentModel.getId());
            return new CommonResponse(false, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user information");
        }
    }

    private User findUserByName(AppointmentModel appointmentModel, String name) {
        return appointmentModel.getUsers().stream()
                .filter(u -> u.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}