package io.licensemanager.backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserRegistrationRequest {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;

    public boolean isValidRequest() {
        return Stream.of(username, password, email, firstName, lastName)
                .noneMatch(StringUtils::isEmpty);
    }
}
