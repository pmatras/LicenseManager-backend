package io.licensemanager.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "customers_groups")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CustomerGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_group_id")
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String name;

    private String displayColor;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "creator_id", referencedColumnName = "id")
    private User creator;
}
