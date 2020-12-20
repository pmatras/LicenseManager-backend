package io.licensemanager.backend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CustomersLicensesStatistics {
    private Long totalLicensesCount;
    private Long validLicensesCount;
    private Long expiredLicensesCount;
}
