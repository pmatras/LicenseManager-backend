package io.licensemanager.backend.configuration.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.licensemanager.backend.entity.Role;
import io.licensemanager.backend.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode
public class AuthenticationDetails implements UserDetails {

    private static Logger logger = LoggerFactory.getLogger(AuthenticationDetails.class);

    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    private Set<Role> roles;
    private String email;
    private Boolean emailConfirmed;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    private Boolean isAccountActivated;
    private boolean isAccountActivatedByAdmin;
    private LocalDateTime creationDate;
    private LocalDateTime lastEditTime;
    private Collection<? extends GrantedAuthority> grantedAuthorities;

    public AuthenticationDetails(final User user, final List<GrantedAuthority> grantedAuthorities) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.roles = user.getRoles();
        this.email = user.getEmail();
        this.emailConfirmed = user.getEmailConfirmed();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.isActive = user.getIsActive();
        this.isAccountActivated = user.getIsAccountActivated();
        this.isAccountActivatedByAdmin = user.getIsAccountActivatedByAdmin();
        this.creationDate = user.getCreationDate();
        this.lastEditTime = user.getLastEditTime();
        this.grantedAuthorities = grantedAuthorities;
    }

    public static AuthenticationDetails build(User user) {
        List<GrantedAuthority> grantedAuthorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return new AuthenticationDetails(user, grantedAuthorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.isActive;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isAccountActivated && this.isAccountActivatedByAdmin;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }
}
