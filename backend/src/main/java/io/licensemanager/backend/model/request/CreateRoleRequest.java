package io.licensemanager.backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Data
public class CreateRoleRequest {
    private String name;
    private Set<String> permissions;

    public boolean isValid() {
        return Stream.of(name,
                permissions.stream().collect(Collectors.joining()))
                .noneMatch(StringUtils::isEmpty);
    }
}
