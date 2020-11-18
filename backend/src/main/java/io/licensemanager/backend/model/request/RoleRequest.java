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
public class RoleRequest {
    private String name;
    private Set<String> permissions;
    private Set<Long> usersIds;

    public boolean isValid() {
        return Stream.of(name,
                permissions.stream().collect(Collectors.joining()))
                .noneMatch(StringUtils::isEmpty);
    }
}
