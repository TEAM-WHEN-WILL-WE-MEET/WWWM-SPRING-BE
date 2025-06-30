package org.example.whenwillwemeet.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApplicationException extends RuntimeException {

  public ErrorCode errorCode;
}
