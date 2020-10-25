package io.licensemanager.backend.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.List;

public class TimeTokensParser {

    private static final Logger logger = LoggerFactory.getLogger(TimeTokensParser.class);

    private static List<Character> periodTimeTokens = List.of('Y', 'M', 'W', 'D');
    private static List<Character> durationTimeTokens = List.of('h', 'm', 's');

    public static TemporalAmount parseTimeToken(String timeToken) {
        if (StringUtils.isBlank(timeToken)) {
            return Period.ZERO;
        }
        Character timeUnitSpecifier = timeToken.charAt(timeToken.length() - 1);
        try {
            if (periodTimeTokens.contains(timeUnitSpecifier)) {
                return Period.parse(String.format("P%s", timeToken));
            }
            if (durationTimeTokens.contains(timeUnitSpecifier)) {
                return Duration.parse(String.format("PT%s", timeToken));
            }
        } catch (DateTimeException e) {
            logger.error("Cannot parse time token, reason: ", e.getMessage());
        }

        return Period.ZERO;
    }
}
