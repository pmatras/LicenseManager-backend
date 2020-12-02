package io.licensemanager.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "licenses")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "license_id")
    private Long id;

    private String name;

    private String licenseFileName;

    @Lob
    private String licenseKey;

    private LocalDateTime generationDate;

    private LocalDateTime expirationDate;

    @JsonIgnore
    @Lob
    private byte[] licenseFile;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "template_id")
    private LicenseTemplate usedTemplate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
