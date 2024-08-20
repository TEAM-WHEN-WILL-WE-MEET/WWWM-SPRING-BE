package org.example.whenwillwemeet.service;

import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.data.dao.AppointmentDAO;
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
                return new CommonResponse(true, HttpStatus.OK, "Login success", existingUser);
            } else {
                // 비밀번호가 불일치하는 경우
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
                return new CommonResponse(true, HttpStatus.CREATED, "New user registered", newUser);
            } else {
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
            return new CommonResponse(false, HttpStatus.NOT_FOUND, "User not found");
        } else if (!passwordEncoder.matches(updatedUser.getPassword(), existingUser.getPassword())) {
            return new CommonResponse(false, HttpStatus.UNAUTHORIZED, "Incorrect password");
        }

        // 업데이트할 정보 설정
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());

        // AppointmentModel 업데이트
        CommonResponse updateResponse = appointmentDAO.updateAppointment(appointmentModel);

        if (updateResponse.isSuccess()) {
            existingUser.setPassword(null);
            return new CommonResponse(true, HttpStatus.OK, "User information updated successfully", existingUser);
        } else {
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