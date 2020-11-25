package io.licensemanager.backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EditCustomerRequest {
    private Long customerId;
    private String newName;
    private Set<String> groups;

    public boolean isValid() {
        return customerId != null && (newName != null || groups != null);
    }
}
