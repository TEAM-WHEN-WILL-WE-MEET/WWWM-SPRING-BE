package org.example.whenwillwemeet.common.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
  @ExceptionHandler(ApplicationException.class)
  protected ResponseEntity<CommonResponse> handleApplicationException(ApplicationException e){
    log.error("{} {}", e, e.getErrorCode().toString());
    return ResponseEntity.status(e.getErrorCode().getHttpStatus())
        .body(new CommonResponse(false, e.getErrorCode().getHttpStatus(), e.getErrorCode().getMessage()));
  }

  @ExceptionHandler(RuntimeException.class)
  protected ResponseEntity<CommonResponse> handleRuntimeException(RuntimeException e) {
    log.error(e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new CommonResponse(false,HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
  }

}
