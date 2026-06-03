package org.emprenApp.shared.application.application;

import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ValidateGeneric {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);


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

    public void validateEmail(String email) throws BaseException {

        if (email == null || email.isBlank()) {
            throw new BaseException(ErrorCodeEnum.PATTERN_EMAIL);
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BaseException(ErrorCodeEnum.PATTERN_EMAIL);
        }
    }
}
