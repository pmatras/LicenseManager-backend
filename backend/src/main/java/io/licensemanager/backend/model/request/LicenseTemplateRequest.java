package io.licensemanager.backend.model.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.licensemanager.backend.configuration.deserialization.LicenseTemplateFieldsConverter;
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
    @JsonDeserialize(converter = LicenseTemplateFieldsConverter.class)
    private Map<String, Class> fields;

    public boolean isValid() {
        return !StringUtils.isBlank(name) &&
                fields != null && !fields.isEmpty();
    }
}
