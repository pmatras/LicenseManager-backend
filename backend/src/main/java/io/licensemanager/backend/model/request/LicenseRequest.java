package io.licensemanager.backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LicenseRequest {
    private String name;
    private String expirationDate;
    private Long templateId;
    private Long customerId;
    private Map<String, Object> values;

    public boolean isValid() {
        return !StringUtils.isBlank(name) &&
                !StringUtils.isBlank(expirationDate) &&
                templateId != null && customerId != null &&
                values != null && !values.isEmpty();
    }
}
