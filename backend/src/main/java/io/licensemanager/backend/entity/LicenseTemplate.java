package io.licensemanager.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.licensemanager.backend.configuration.serialization.ClassToSimpleNameConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "license_templates")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LicenseTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "license_template_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @JsonSerialize(contentConverter = ClassToSimpleNameConverter.class)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "license_templates_fields",
            joinColumns = {@JoinColumn(name = "license_template_id", referencedColumnName = "license_template_id")}
    )
    @MapKeyColumn(name = "field_name")
    @Column(name = "field_type")
    private Map<String, Class> fields;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.MERGE)
    private User creator;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Basic
    private LocalDateTime creationTime;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Basic
    private LocalDateTime editionTime;

    @JsonIgnore
    private PublicKey publicKey;

    @JsonIgnore
    private PrivateKey privateKey;
}
