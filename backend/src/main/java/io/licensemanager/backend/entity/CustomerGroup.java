package io.licensemanager.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Set;

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

    @ManyToMany(
            cascade = {CascadeType.MERGE},
            fetch = FetchType.LAZY
    )
    @JoinTable(
            name = "customers_customers_groups",
            joinColumns = @JoinColumn(name = "customer_group_id"),
            inverseJoinColumns = @JoinColumn(name = "customer_id")
    )
    private Set<Customer> customers;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User creator;
}
