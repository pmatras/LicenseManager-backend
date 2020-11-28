package io.licensemanager.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "license_templates_fields",
            joinColumns = {@JoinColumn(name = "license_template_id", referencedColumnName = "license_template_id")}
    )
    @MapKeyColumn(name = "field_name")
    @Column(name = "field_type")
    private Map<String, Class> fields;

    @OneToOne(cascade = CascadeType.MERGE)
    private User creator;

    @Basic
    private LocalDateTime creationTime;

    @Basic
    private LocalDateTime editionTime;
}
