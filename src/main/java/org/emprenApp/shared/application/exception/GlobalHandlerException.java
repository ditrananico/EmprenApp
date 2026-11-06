package org.emprenApp.shared.application.exception;

import org.emprenApp.shared.application.BaseResponse;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalHandlerException extends ResponseEntityExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalHandlerException.class);

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
    public ResponseEntity<Object> handleValidationException(ValidationException ex) {
        ErrorCodeEnum code = ErrorCodeEnum.getCode(ex.getCode());
        return new ResponseEntity<>(new BaseResponse(code), statusFor(code));
    }

    // Cualquier BaseException "pura" (INVALID_PARAMETERS, DUPLICATE_KEY, INPUT_LENGTH...).
    // Las subclases con handler propio (NotFound, Generic, Validation) siguen usando el suyo.
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Object> handleBaseException(BaseException ex) {
        ErrorCodeEnum code = ErrorCodeEnum.getCode(ex.getCode());
        this.logger.warn("BaseException: {}", ex.getError());
        return new ResponseEntity<>(new BaseResponse(code), statusFor(code));
    }

    // Errores que Spring detecta antes de llegar al servicio: se responden con el mismo formato { "code": ... }.
    // Los demas (405, 415, etc.) conservan el comportamiento por defecto de Spring.
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        return invalidParameters("Validacion de request fallida: {}", ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        return invalidParameters("JSON mal formado: {}", ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {
        return invalidParameters("Tipo de parametro incorrecto: {}", ex.getMessage());
    }

    private ResponseEntity<Object> invalidParameters(String logMessage, String detail) {
        this.logger.warn(logMessage, detail);
        return new ResponseEntity<>(new BaseResponse(ErrorCodeEnum.INVALID_PARAMETERS), HttpStatus.BAD_REQUEST);
    }

    private static HttpStatus statusFor(ErrorCodeEnum code) {
        if (code == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return switch (code) {
            case INVALID_PARAMETERS, PATTERN_EMAIL, INPUT_LENGTH, ID_ERROR,
                 INVALID_PRICE, INVALID_STOCK, PEDIDO_ERROR_CANCELAR -> HttpStatus.BAD_REQUEST;
            case INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DUPLICATE_KEY -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
