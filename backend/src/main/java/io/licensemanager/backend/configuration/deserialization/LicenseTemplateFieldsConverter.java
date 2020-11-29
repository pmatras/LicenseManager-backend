package io.licensemanager.backend.configuration.deserialization;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import io.licensemanager.backend.configuration.setup.SUPPORTED_FIELD_TYPES;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class LicenseTemplateFieldsConverter implements Converter<Map<String, String>, Map<String, Class>> {

    private static final Logger logger = LoggerFactory.getLogger(LicenseTemplateFieldsConverter.class);

    @Override
    public Map<String, Class> convert(Map<String, String> value) {
        Map<String, Class> converted = new HashMap<>();
        value.forEach((fieldName, fieldType) -> {
            try {
                if (SUPPORTED_FIELD_TYPES.isTypeSupported(fieldType)) {
                    converted.put(fieldName, Class.forName(String.format("java.lang.%s", fieldType)));
                } else {
                    logger.error("Type {} isn't supported - will be omitted", fieldType);
                }
            } catch (ClassNotFoundException e) {
                logger.error("Exception occurred during deserialization - {} isn't supported type name, will be skipped", fieldType);
                logger.debug("Exception: ", e);
            }
        });

        return converted;
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructMapType(Map.class, String.class, String.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructMapType(Map.class, String.class, Class.class);
    }
}
