package org.emprenApp.shared.application.enums;

import java.util.Arrays;

public enum ErrorCodeEnum {

    OK(0, "Accion realizada correctamente"),
    PARAMETROS_INCORRECTOS(1, "Parametros ingresados incorrectos"),
    NOT_FOUND(2, "Recurso no encontrado"),
    DUPLICATE_KEY(3, "Clave unica duplicada"),
    PATTERN_EMAIL(4, "Correo electronico no valido"),
    GENERIC_ERROR(99, "Error generico en el sistema");

    private final Integer code;
    private final String description;

    private ErrorCodeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public static ErrorCodeEnum getCode(Integer code) {
        return (ErrorCodeEnum) Arrays.stream(values()).filter((error) -> {
            return error.getCode().equals(code);
        }).findFirst().orElse(null);
    }
}
