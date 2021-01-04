package io.licensemanager.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class License {
    private String customerName;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime creationDate;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime expirationDate;

    @JsonProperty("float_field")
    private float floatField;
    @JsonProperty("bool_field")
    private boolean boolField;
    @JsonProperty("long_field")
    private long longField;
    @JsonProperty("char_field")
    private char charField;
    @JsonProperty("int_field")
    private int intField;
    @JsonProperty("double_field")
    private double doubleField;
    @JsonProperty("string_field")
    private String stringField;

    public static License parseJson(final String json) {
        License license = new License();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            license = objectMapper.readValue(json, License.class);
        } catch (JsonProcessingException e) {
            System.out.println(String.format("Failed to parse JSON, reason - %s", e.getMessage()));
        }

        return license;
    }
}
