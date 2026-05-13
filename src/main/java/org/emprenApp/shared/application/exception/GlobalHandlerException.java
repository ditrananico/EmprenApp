package org.emprenApp.shared.application.exception;

import org.emprenApp.shared.application.BaseResponse;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalHandlerException extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        this.logger.error("CRITIC Error Exception:", ex);
        return new ResponseEntity<>(new BaseResponse(ErrorCodeEnum.GENERIC_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleNullPointerException(NullPointerException ex) {
        this.logger.error("Error NullPointerException: ", ex);
        return new ResponseEntity<>(new BaseResponse(ErrorCodeEnum.GENERIC_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        this.logger.error("Error RuntimeException: ", ex);
        return new ResponseEntity<>(new BaseResponse(ErrorCodeEnum.GENERIC_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException ex) {
        this.logger.error(ex.getMessage(), ex);
        return new ResponseEntity<>(new BaseResponse(ErrorCodeEnum.getCode(ex.getCode())), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GenericException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleGenericException(GenericException ex) {
        this.logger.error("Error GenericException: ", ex);
        return new ResponseEntity<>(new BaseResponse(ErrorCodeEnum.getCode(ex.getCode())), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ResponseEntity<Object> handleValidationException(ValidationException ex) {
        return new ResponseEntity<>(new BaseResponse(ErrorCodeEnum.getCode(ex.getCode())), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
