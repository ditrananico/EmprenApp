package org.emprenApp.shared.application.enums;

import java.util.Arrays;

public enum ErrorCodeEnum {

    OK(0, "Accion realizada correctamente"),
    INVALID_PARAMETERS(1, "Parametros ingresados incorrectos"),
    NOT_FOUND(2, "Recurso no encontrado"),
    DUPLICATE_KEY(3, "Clave unica duplicada"),
    PATTERN_EMAIL(4, "Correo electronico no valido"),
    INPUT_LENGTH(5, "El campo supera el máximo permitido"),
    PEDIDO_ERROR_CANCELAR(6, "El pedido no se pudo cancelar"),
    ID_ERROR(7, "El ID debe ser un número positivo"),
    INVALID_PRICE(8,"El precio debe ser un número positivo"),
    INVALID_STOCK(9,"El stock debe ser un número positivo"),
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
