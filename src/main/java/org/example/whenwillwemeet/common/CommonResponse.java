package org.example.whenwillwemeet.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
public class CommonResponse {
    private boolean isSuccess;
    HttpStatus status;
    private String msg;
    private Object object;

    public CommonResponse(boolean isSuccess, String msg) {
        this.isSuccess = isSuccess;
        this.msg = msg;
    }

    public CommonResponse(boolean isSuccess, HttpStatus status, String msg) {
        this.isSuccess = isSuccess;
        this.status = status;
        this.msg = msg;
    }

    public CommonResponse(boolean isSuccess, HttpStatus status, String msg, Object object) {
        this.isSuccess = isSuccess;
        this.status = status;
        this.msg = msg;
        this.object = object;
    }
}