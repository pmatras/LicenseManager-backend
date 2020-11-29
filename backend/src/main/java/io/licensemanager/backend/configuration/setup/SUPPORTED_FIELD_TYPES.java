package io.licensemanager.backend.configuration.setup;

import java.util.stream.Stream;

public enum SUPPORTED_FIELD_TYPES {
    String,
    Integer,
    Long,
    Float,
    Double,
    Boolean,
    Character;

    public static boolean isTypeSupported(final String typeName) {
        return Stream.of(SUPPORTED_FIELD_TYPES.values())
                .map(Enum::name)
                .anyMatch(supportedTypeName -> supportedTypeName.equals(typeName));
    }
}
