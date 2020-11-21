package io.licensemanager.backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CustomerGroupRequest {
    private String name;
    private String displayColor;
    private Set<Long> customersIds;

    public boolean isValid() {
        return !StringUtils.isBlank(name);
    }
}
