package org.emprenApp.shared.application.application;

import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

public class ValidateGeneric {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);


    public static void validateId(Long id) throws BaseException {
        if (id == null || id <= 0) {
            throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
        }
    }

    public static <T> void validateNotNull(T object) throws BaseException {
        if (object == null) {
            throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
        }
    }

    public static void validateEmail(String email) throws BaseException {

        if (email == null || email.isBlank()) {
            throw new BaseException(ErrorCodeEnum.PATTERN_EMAIL);
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BaseException(ErrorCodeEnum.PATTERN_EMAIL);
        }
    }

    public static void validateNotBlank(String text) throws BaseException {
        if (text == null || text.isBlank()) {
            throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
        }
    }

    public static void validateMaxLength(String text, int max) throws BaseException {
        if (text != null && text.length() > max) {
            throw new BaseException(ErrorCodeEnum.INPUT_LENGTH);
        }
    }
}
