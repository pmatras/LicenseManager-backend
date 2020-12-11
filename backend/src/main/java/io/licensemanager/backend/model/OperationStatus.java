package io.licensemanager.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OperationStatus {
    private Boolean status;
    private String message;
}
