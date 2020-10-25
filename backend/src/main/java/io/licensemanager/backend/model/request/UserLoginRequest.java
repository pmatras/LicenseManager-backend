package io.licensemanager.backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserLoginRequest {
    private String username;
    private String password;
}
