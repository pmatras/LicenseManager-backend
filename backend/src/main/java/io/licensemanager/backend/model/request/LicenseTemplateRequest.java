package io.licensemanager.backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LicenseTemplateRequest {
    private String name;
    private Map<String, String> fields;

    public boolean isValid() {
        return !StringUtils.isBlank(name) &&
                fields != null && !fields.isEmpty();
    }
}
