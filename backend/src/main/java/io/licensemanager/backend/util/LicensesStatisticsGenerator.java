package io.licensemanager.backend.util;

import io.licensemanager.backend.entity.License;
import io.licensemanager.backend.model.response.LicensesStatistics;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class LicensesStatisticsGenerator {
    public static LicensesStatistics generateStats(final List<License> licenses) {
        LicensesStatistics licensesStatistics = new LicensesStatistics();

        licensesStatistics.setTotalCount(
                licenses.stream()
                        .count()
        );
        licensesStatistics.setActiveCount(
                licenses.stream()
                        .filter(license -> license.getIsActive())
                        .count()
        );
        licensesStatistics.setInactiveCount(
                licenses.stream()
                        .filter(license -> !license.getIsActive())
                        .count()
        );
        licensesStatistics.setValidCount(
                licenses.stream()
                        .filter(license -> !license.getIsExpired())
                        .count()
        );
        licensesStatistics.setExpiredCount(
                licenses.stream()
                        .filter(license -> license.getIsExpired())
                        .count()
        );
        licensesStatistics.setByCustomers(
                licenses.stream()
                        .collect(Collectors.groupingBy(license -> license.getCustomer().getName(), Collectors.counting()))
        );
        licensesStatistics.setByTemplates(
                licenses.stream()
                        .collect(Collectors.groupingBy(license -> license.getUsedTemplate().getName(), Collectors.counting()))
        );
        licensesStatistics.setByGenerationDate(
                licenses.stream()
                        .collect(Collectors.groupingBy(license -> license.getGenerationDate().truncatedTo(ChronoUnit.DAYS), Collectors.counting()))
        );
        licensesStatistics.setByGenerationDateMonths(
                licenses.stream()
                        .collect(Collectors.groupingBy(license -> license.getGenerationDate().truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1), Collectors.counting()))
        );
        licensesStatistics.setByExpirationDate(
                licenses.stream()
                        .collect(Collectors.groupingBy(license -> license.getExpirationDate().truncatedTo(ChronoUnit.DAYS), Collectors.counting()))
        );
        licensesStatistics.setByExpirationDateMonths(
                licenses.stream()
                        .collect(Collectors.groupingBy(license -> license.getExpirationDate().truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1), Collectors.counting()))
        );

        return licensesStatistics;
    }
}
