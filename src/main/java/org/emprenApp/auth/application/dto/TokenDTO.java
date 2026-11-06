package org.emprenApp.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenDTO {

    @ToString.Exclude
    private String token;
    private String tokenType;
    private Long expiresIn;

}
