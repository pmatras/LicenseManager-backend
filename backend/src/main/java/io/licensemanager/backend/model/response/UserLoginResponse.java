package io.licensemanager.backend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
public class UserLoginResponse {
    private String message;
    private String authorizationToken;
    private User user;

    public UserLoginResponse(String message, String authorizationToken, String username, String firstName, String lastName,
                             Set<String> roles, Set<String> privileges) {
        this.message = message;
        this.authorizationToken = authorizationToken;
        this.user = new User(
                username,
                firstName,
                lastName,
                roles,
                privileges
        );
    }
}

@AllArgsConstructor
@Data
class User {
    private String username;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private Set<String> privileges;
}