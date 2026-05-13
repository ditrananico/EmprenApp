package org.emprenApp.shared.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse {

    private ErrorCodeEnum code;

}
