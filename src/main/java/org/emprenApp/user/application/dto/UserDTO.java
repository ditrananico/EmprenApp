package org.emprenApp.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.emprenApp.shared.application.enums.UserStatusEnum;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;
    private String email;
    private Timestamp createdAt;
    private UserStatusEnum status;
    private String firstName;
    private String lastName;
    private String phone;
    private Timestamp emailVerifiedAt;
    private Timestamp phoneVerifiedAt;

    public String toString(){
        return "UserDTO{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", status=" + status +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", emailVerifiedAt=" + emailVerifiedAt +
                ", phoneVerifiedAt=" + phoneVerifiedAt +
                '}';
    }
}
