package io.licensemanager.backend.util;

import io.licensemanager.backend.configuration.setup.ROLES_PERMISSIONS;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthenticationUtils {
    private static String ROLE_PREFIX = "ROLE_";
    private static String PERMISSION_PREFIX = "PERMISSION_";

    public static String parseUsername(final Authentication authentication) {
        return ((UserDetails) authentication.getPrincipal()).getUsername();
    }

    public static Set<ROLES_PERMISSIONS> parsePermissions(final Authentication authentication) {
        return authentication.getAuthorities().stream()
                .filter(authority -> !authority.getAuthority().startsWith(ROLE_PREFIX))
                .map(authority -> {
                    String permission = StringUtils.removeStart(authority.getAuthority(), PERMISSION_PREFIX);
                    return EnumUtils.getEnum(ROLES_PERMISSIONS.class, permission, null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static Set<String> parseRoles(final Authentication authentication) {
        return authentication.getAuthorities().stream()
                .filter(authority -> !authority.getAuthority().startsWith(PERMISSION_PREFIX))
                .map(authority ->
                        StringUtils.removeStart(authority.getAuthority(), ROLE_PREFIX)
                )
                .collect(Collectors.toSet());
    }
}
