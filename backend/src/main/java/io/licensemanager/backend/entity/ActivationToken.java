package io.licensemanager.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
@Table(name = "activation_tokens")
public class ActivationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String value;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Basic
    private LocalDateTime creationDate;

    @Basic
    private LocalDateTime expirationDate;

    private String IP;

    private String localization;

}
