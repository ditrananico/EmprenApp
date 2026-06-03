package org.emprenApp.shared.application.application;

import org.emprenApp.shared.application.exception.GenericException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class BaseRestController {

    protected <T> ResponseEntity<T> responseOk(T responseDto) {
        return ResponseEntity.ok(responseDto);
    }

    protected <T> ResponseEntity<T> responseCreated(T responseDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    protected ResponseEntity<String> responseMessage(String mensaje) {
        return ResponseEntity.ok(mensaje);
    }

    protected ResponseEntity<String> responseError(GenericException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mensajeError(e));
    }

    private String mensajeError(GenericException e) {
         return e.getError();
    }


}
