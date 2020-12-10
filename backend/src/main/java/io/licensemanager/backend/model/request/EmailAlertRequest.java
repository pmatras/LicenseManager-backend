package io.licensemanager.backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EmailAlertRequest {
    private Integer threshold;
    private String activeHoursFrom;
    private String activeHoursTo;

    public boolean isValid() {
        return threshold != null && threshold > 0 &&
                Stream.of(activeHoursFrom,
                        activeHoursTo)
                        .noneMatch(StringUtils::isEmpty);
    }
}
