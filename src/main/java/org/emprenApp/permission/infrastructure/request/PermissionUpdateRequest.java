package org.emprenApp.permission.infrastructure.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermissionUpdateRequest {

    @NotNull
    private Long id;

    private String name;

    private String description;
}
