package org.example.whenwillwemeet.service;

import org.example.whenwillwemeet.common.CommonResponse;
import org.example.whenwillwemeet.data.dao.AppointmentDAO;
import org.example.whenwillwemeet.data.model.AppointmentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService {
    @Autowired
    private AppointmentDAO appointmentDAO;

    public CommonResponse createAppointment(AppointmentModel appointmentModel){
        return appointmentDAO.createAppointment(appointmentModel);
    }
}
