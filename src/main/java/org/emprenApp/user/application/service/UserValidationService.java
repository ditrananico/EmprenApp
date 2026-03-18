package org.emprenApp.user.application.service;

import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.GenericException;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    protected void validateEmail(String email) throws GenericException {

        if (email == null || email.isBlank()) {
            throw new GenericException(ErrorCodeEnum.PATTERN_EMAIL);
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new GenericException(ErrorCodeEnum.PATTERN_EMAIL);
        }
    }
}
