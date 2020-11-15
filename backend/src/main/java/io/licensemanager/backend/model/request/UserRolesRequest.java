package io.licensemanager.backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserRolesRequest {
    private Long userId;
    private Set<String> roles;

    public boolean isValid() {
        return Stream.of(userId.toString(),
                roles.stream().collect(Collectors.joining()))
                .noneMatch(StringUtils::isEmpty);
    }
}
