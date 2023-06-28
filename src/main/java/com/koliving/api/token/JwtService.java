package com.koliving.api.token;

import com.koliving.api.token.refresh.RefreshToken;
import com.koliving.api.token.refresh.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.xml.bind.DatatypeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JwtService implements IJwtService {

    private String jwtSecret;
    private final UserDetailsService userService;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtService(@Value("${jwt.secret}") String jwtSecret,
                      UserDetailsService userService,
                      RefreshTokenRepository refreshTokenRepository) {
        this.jwtSecret = jwtSecret;
        this.userService = userService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public String extractEmail(String token) {
        Claims claim = getClaims(token);
        return (String) claim.get("email");
    }

    @Override
    public Authentication getAuthentication(String accessToken) {
        String email = this.extractEmail(accessToken);
        UserDetails userDetails = userService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    @Override
    @Transactional
    public String saveRefreshToken(String email, String newRefreshTokenValue) {
        RefreshToken newRefreshToken = RefreshToken.builder()
                .email(email)
                .refreshToken(newRefreshTokenValue)
                .build();

        return refreshTokenRepository.save(newRefreshToken);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(jwtSecret))
                .parseClaimsJws(token)
                .getBody();
    }
}
