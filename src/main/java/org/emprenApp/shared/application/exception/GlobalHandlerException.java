package org.emprenApp.shared.application.exception;

import org.emprenApp.shared.application.BaseResponse;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalHandlerException extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        this.logger.error(ex.getMessage(), ex);
        return new ResponseEntity<>(new BaseResponse(ErrorCodeEnum.GENERIC_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
