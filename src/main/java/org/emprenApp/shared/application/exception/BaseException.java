package org.emprenApp.shared.application.exception;

import org.emprenApp.shared.application.enums.ErrorCodeEnum;

public class BaseException extends Exception{
    private String message = ErrorCodeEnum.OK.getDescription();
    private Integer code = ErrorCodeEnum.OK.getCode();

    public BaseException() {}

    public BaseException(ErrorCodeEnum errorCodeEnum) {
        this.message = errorCodeEnum.getDescription();
        this.code = errorCodeEnum.getCode();
    }

    public String getError(){
        return "code" + code + " - " + message + "";
    }
}
