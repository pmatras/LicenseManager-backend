package io.licensemanager.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

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

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generationDate;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expirationDate;

    @JsonIgnore
    @Lob
    private byte[] licenseFile;

    @JsonIgnoreProperties({"creationTime", "editionTime"})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "template_id")
    private LicenseTemplate usedTemplate;

    @JsonIgnoreProperties({"groups", "creationDate", "lastModificationDate"})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Type(type = "yes_no")
    private Boolean isExpired;

    @Type(type = "yes_no")
    private Boolean isActive;
}
