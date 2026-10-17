package org.emprenApp.role.infrastructure.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RoleUpdateRequest {

    @NotNull
    private Long id;

    private String name;

    private String description;

    private List<Long> permissionIds;
}
