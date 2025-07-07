package org.example.whenwillwemeet.common.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConstantVariables {

    // Appointment 만료 시간 (현재 30days)
    @Value( "${constant.appointment-expiration-time}")
    public static int APPOINTMENT_EXPIRATION_TIME;
    // 타임 슬롯 시간 간격 (분)
    @Value( "${constant.time-slot-unit-time}")
    public static int TIME_SLOT_UNIT_TIME;
}
