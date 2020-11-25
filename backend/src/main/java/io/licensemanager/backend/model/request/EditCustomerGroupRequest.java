package io.licensemanager.backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EditCustomerGroupRequest {
    private Long groupId;
    private String newName;
    private String newDisplayColor;

    public boolean isValid() {
        return groupId != null &&
                (newName != null || newDisplayColor != null);
    }
}
