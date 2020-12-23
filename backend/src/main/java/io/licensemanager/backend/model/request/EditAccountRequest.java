package io.licensemanager.backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EditAccountRequest {
    private String currentPassword = "";
    private Optional<String> username = Optional.empty();
    private Optional<String> password = Optional.empty();
}
