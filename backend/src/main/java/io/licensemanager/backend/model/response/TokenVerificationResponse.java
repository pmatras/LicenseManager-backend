package io.licensemanager.backend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@AllArgsConstructor
@Data
public class TokenVerificationResponse {
    private String username;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private Set<String> privileges;
}
