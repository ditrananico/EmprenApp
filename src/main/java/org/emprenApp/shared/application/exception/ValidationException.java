package org.emprenApp.shared.application.exception;

import org.emprenApp.shared.application.enums.ErrorCodeEnum;

public class ValidationException extends BaseException {
    public ValidationException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum);
    }
    public ValidationException() {
        super(ErrorCodeEnum.INVALID_PARAMETERS);
    }

}