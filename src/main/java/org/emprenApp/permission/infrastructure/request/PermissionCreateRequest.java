package org.emprenApp.permission.infrastructure.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermissionCreateRequest {

    @NotNull
    private String name;

    private String description;
}
