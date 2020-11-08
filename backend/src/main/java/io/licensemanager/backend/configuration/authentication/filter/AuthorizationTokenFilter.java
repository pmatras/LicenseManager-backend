package io.licensemanager.backend.configuration.authentication.filter;

import io.licensemanager.backend.configuration.authentication.service.AuthenticationDetailsService;
import io.licensemanager.backend.configuration.authentication.service.AuthorizationTokenService;
import io.licensemanager.backend.entity.Token;
import io.licensemanager.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthorizationTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationTokenFilter.class);

    private final AuthorizationTokenService tokenService;
    private final AuthenticationDetailsService authenticationDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.debug("Parsing authorization token from request...");
        try {
            Optional<String> tokenValue = tokenService.parseTokenFromRequest(request);
            if (tokenValue.isPresent()) {
                logger.debug("Found authorization token in request's header");
                Optional<Token> token = tokenService.findTokenByValue(tokenValue.get());
                if (token.isPresent() && tokenService.isTokenValid(token.get())) {
                    logger.debug("Authorization token is valid");
                    User user = token.get().getUser();

                    UserDetails userDetails = authenticationDetailsService.loadUserByUsername(user.getUsername());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, userDetails.getPassword(), userDetails.getAuthorities()
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else if (!token.isPresent()) {
                    logger.warn("Detected fraud or already purged token in request's header");
                } else {
                    logger.error("Detected request with expired authorization token");
                }
            } else {
                logger.warn("Detected request without authorization token for request: {} {}",
                        request.getMethod(),
                        request.getRequestURL());
            }
        } catch (Exception e) {
            logger.error("Exception occurred while parsing authorization token from request, reason: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
