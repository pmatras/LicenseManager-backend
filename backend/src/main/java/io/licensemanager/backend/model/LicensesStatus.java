package io.licensemanager.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LicensesStatus {
    private Long validLicensesCount = 0L;
    private Long expiredLicensesCount = 0L;

    public void incrementValidLicensesCount() {
        ++this.validLicensesCount;
    }

    public void incrementExpiredLicensesCount() {
        ++this.expiredLicensesCount;
    }
}
