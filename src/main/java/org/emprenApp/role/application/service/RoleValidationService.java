package org.emprenApp.role.application.service;

import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.exception.BaseException;
import org.springframework.stereotype.Service;

@Service
public class RoleValidationService {

    private static final int NAME_MAX = 50;
    private static final int DESCRIPTION_MAX = 255;

    public void validateFields(String name, String description) throws BaseException {
        if (name != null) {
            ValidateGeneric.validateNotBlank(name);
            ValidateGeneric.validateMaxLength(name.trim(), NAME_MAX);
        }
        ValidateGeneric.validateMaxLength(description, DESCRIPTION_MAX);
    }
}
