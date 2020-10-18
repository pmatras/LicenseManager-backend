package io.licensemanager.backend.configuration.authentication.service;

import io.licensemanager.backend.entity.Token;
import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthorizationTokenService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationTokenService.class);

    private final int TOKEN_BYTES = 256;

    private final TokenRepository tokenRepository;

    public Optional<String> parseTokenFromRequest(HttpServletRequest request, final String authHeader, final String tokenType) {
        String authorizationHeader = request.getHeader(authHeader);
        if (!StringUtils.isEmpty(authorizationHeader) && authorizationHeader.startsWith(tokenType)) {
            return Optional.of(StringUtils.removeStart(authorizationHeader, tokenType).trim());
        }

        return Optional.empty();
    }

    public Optional<Token> findTokenByValue(final String value) {
        return tokenRepository.findByValue(value);
    }

    public boolean isTokenValid(Token token) {
        return token.getExpirationDate().isBefore(LocalDateTime.now());
    }

    public String generateToken() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);

        return Sha512DigestUtils.shaHex(bytes);
    }

    public void assignTokenToUser(String tokenValue, String userAgent, User user) {
        logger.debug("Assigning new token to user with username {}", user.getUsername());
        Token token = new Token();
        token.setValue(tokenValue);
        LocalDateTime currentDate = LocalDateTime.now();
        token.setExpirationDate(currentDate.plusHours(24L));
        token.setCreationDate(currentDate);
        token.setUser(user);
        token.setUserUA(userAgent);

        tokenRepository.save(token);
    }

    public void revokeTokenFromUser(User user) {
        logger.debug("Revoking token from user with username {}", user.getUsername());
        Optional<Token> token = tokenRepository.findByUserId(user.getId());
        token.ifPresent(tokenToDelete -> tokenRepository.delete(tokenToDelete));
    }
}
