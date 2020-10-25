package io.licensemanager.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @ManyToMany(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY
    )
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = Collections.EMPTY_SET;

    @Email(message = "Please enter valid e-mail address")
    @Column(nullable = false, unique = true)
    private String email;

    @Type(type = "yes_no")
    private Boolean emailConfirmed;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Type(type = "yes_no")
    private Boolean isActive;

    @Type(type = "yes_no")
    private Boolean isAccountActivated;

    @Type(type = "yes_no")
    private Boolean isAccountActivatedByAdmin;

    @Basic
    private LocalDateTime creationDate;

    @Basic
    private LocalDateTime lastEditTime;
}
