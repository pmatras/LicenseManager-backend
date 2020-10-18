package io.licensemanager.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "tokens")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String value;

    @Basic
    private LocalDateTime creationDate;

    @Basic
    private LocalDateTime expirationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String userUA;
}
