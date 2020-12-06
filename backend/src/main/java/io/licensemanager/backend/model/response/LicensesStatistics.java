package io.licensemanager.backend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LicensesStatistics {
    private Long totalCount;
    private Long activeCount;
    private Long inactiveCount;
    private Long validCount;
    private Long expiredCount;
    private Map<String, Long> byCustomers;
    private Map<String, Long> byTemplates;
    private Map<LocalDateTime, Long> byGenerationDate;
    private Map<LocalDateTime, Long> byGenerationDateMonths;
    private Map<LocalDateTime, Long> byExpirationDate;
    private Map<LocalDateTime, Long> byExpirationDateMonths;
}
