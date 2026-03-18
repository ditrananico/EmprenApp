package org.emprenApp.shared.application.exception;

import org.emprenApp.shared.application.enums.ErrorCodeEnum;

public class NotFoundException extends BaseException{

    public NotFoundException() {
        super(ErrorCodeEnum.NOT_FOUND);
    }
}
