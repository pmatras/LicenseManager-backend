package io.licensemanager.backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CustomerRequest {
    private String name;
    private Set<String> groups;

    public boolean isValid() {
        return !StringUtils.isBlank(name);
    }
}
