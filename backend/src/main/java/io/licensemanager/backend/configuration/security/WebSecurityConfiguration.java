package io.licensemanager.backend.configuration.security;

import io.licensemanager.backend.configuration.authentication.filter.AuthorizationTokenFilter;
import io.licensemanager.backend.configuration.authentication.filter.UnauthorizedHandler;
import io.licensemanager.backend.configuration.authentication.service.AuthenticationDetailsService;
import io.licensemanager.backend.configuration.authentication.service.AuthorizationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final AuthenticationDetailsService authenticationDetailsService;
    private final AuthorizationTokenService tokenService;
    private final UnauthorizedHandler unauthorizedHandler;

    @Bean
    public AuthorizationTokenFilter authorizationTokenFilter() {
        return new AuthorizationTokenFilter(tokenService, authenticationDetailsService);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(authenticationDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf().ignoringAntMatchers("/api/auth/register", "/api/account/activate**", "/api/auth/login", "/api/auth/token", "/api/auth/logout")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/auth/register", "/api/account/activate**", "/api/auth/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
                .and().formLogin().disable();

        http.addFilterBefore(authorizationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
