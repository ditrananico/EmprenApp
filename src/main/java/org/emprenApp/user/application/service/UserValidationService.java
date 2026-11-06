package org.emprenApp.user.application.service;

import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserValidationService {

    private static final int EMAIL_MAX = 255;
    private static final int NAME_MAX = 100;
    private static final int PHONE_MAX = 20;
    private static final int PASSWORD_MIN = 8;
    private static final int PASSWORD_MAX = 50;

    // opcional "+" seguido de 7 a 19 digitos, sin espacios ni guiones.
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{7,19}$");

    public void validateCreate(String email, String password, String firstName, String lastName, String phone) throws BaseException {
        validateEmail(email);
        validatePassword(password);
        ValidateGeneric.validateNotBlank(firstName);
        ValidateGeneric.validateNotBlank(lastName);
        ValidateGeneric.validateNotBlank(phone);
        validateUpdate(firstName, lastName, phone);
    }

    // Formato y largo; valida sobre el email recortado (null o vacio -> PATTERN_EMAIL).
    public void validateEmail(String email) throws BaseException {
        ValidateGeneric.validateEmail(email);
        ValidateGeneric.validateMaxLength(email.trim(), EMAIL_MAX);
    }

    // En el update un campo null significa "no cambiar"; si viene, no puede estar vacio.
    public void validateUpdate(String firstName, String lastName, String phone) throws BaseException {
        if (firstName != null) {
            ValidateGeneric.validateNotBlank(firstName);
            ValidateGeneric.validateMaxLength(firstName.trim(), NAME_MAX);
        }
        if (lastName != null) {
            ValidateGeneric.validateNotBlank(lastName);
            ValidateGeneric.validateMaxLength(lastName.trim(), NAME_MAX);
        }
        if (phone != null) {
            ValidateGeneric.validateNotBlank(phone);
            ValidateGeneric.validateMaxLength(phone.trim(), PHONE_MAX);
            if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
                throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
            }
        }
    }

    private void validatePassword(String password) throws BaseException {
        ValidateGeneric.validateNotBlank(password);
        if (password.length() < PASSWORD_MIN) {
            throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
        }
        ValidateGeneric.validateMaxLength(password, PASSWORD_MAX);
    }
}
