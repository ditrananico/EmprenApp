package org.emprenApp.shared.application.exception;

import org.emprenApp.shared.application.enums.ErrorCodeEnum;

public class GenericException extends BaseException{

    public GenericException() {
        super(ErrorCodeEnum.GENERIC_ERROR);
    }

    public GenericException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum);
    }
}
