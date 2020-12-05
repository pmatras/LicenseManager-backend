package io.licensemanager.backend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LicenseFileContentResponse {
    private boolean isVerified;
    private String fileContent;
}
