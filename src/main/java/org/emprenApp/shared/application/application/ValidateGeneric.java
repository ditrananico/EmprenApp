package org.emprenApp.shared.application.application;

import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.springframework.stereotype.Component;

@Component
public class ValidateGeneric {

    public void validateId(Long id) throws BaseException {
        if (id == null || id <= 0) {
            throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
        }
    }

    public <T> void validateNotNull(T object) throws BaseException {
        if (object == null) {
            throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
        }
    }
}
