package com.koliving.api.token;

import com.koliving.api.clock.IClock;
import com.koliving.api.email.IEmailService;
import com.koliving.api.email.MailType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConfirmationTokenService implements IConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final IEmailService emailService;
    private final IClock clock;
    private String origin;
    private String currentVersion;
    private long validityPeriod;

    public ConfirmationTokenService(ConfirmationTokenRepository confirmationTokenRepository,
                                    IEmailService emailService,
                                    IClock clock,
                                    @Value("${server.origin:http://localhost:8080}") String origin,
                                    @Value("${server.current-version:v1}") String currentVersion,
                                    @Value("${spring.mail.properties.mail.auth.validity-period:30}") long validityPeriod
                                    ) {
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.emailService = emailService;
        this.clock = clock;
        this.origin = origin;
        this.currentVersion = currentVersion;
        this.validityPeriod = validityPeriod;
    }

    @Override
    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    @Override
    public ConfirmationToken createToken(String email) {
        return ConfirmationToken.builder()
                .email(email)
                .validityPeriod(validityPeriod)
                .build();
    }

    @Override
    @Transactional
    public ConfirmationToken saveToken(ConfirmationToken token) {
        return confirmationTokenRepository.save(token);
    }

    @Override
    public void sendEmail(String email, String token) {
        String authLinkPath = String.format("/api/%s/signup/confirm", currentVersion);
        String tokenValue = token;
        String authLink = origin + authLinkPath + "?token=" + tokenValue + "&email=" + email;

        emailService.send(MailType.AUTH, email, authLink);
    }

    @Override
    @Transactional
    public String authenticateToken(String token) {
        Optional<ConfirmationToken> optionalConfirmationToken = getToken(token);
        if (optionalConfirmationToken.isEmpty()) {
            this.handleInvalidToken();
        }

        optionalConfirmationToken
                .filter(this::isNotExpired)
                .filter(this::isNotConfirmed)
                .ifPresent(this::confirmToken);

        //TODO response : 302 -> signup-password page (The token successfully confirmed)
        return "302";
    }

    private boolean isNotExpired(ConfirmationToken confirmationToken) {
        LocalDateTime expiresAt = confirmationToken.getExpiresAt();
        if (isExpired(expiresAt)) {
            // TODO : 401 (The token has expired)
            throw new IllegalStateException("token has expired");
        }
        return true;
    }

    private boolean isNotConfirmed(ConfirmationToken confirmationToken) {
        if (confirmationToken.isConfirmed()) {
            // TODO : 401 (The token already confirmed)
            throw new IllegalStateException("token already confirmed");
        }
        return true;
    }

    private void handleInvalidToken() {
        // TODO : 401 (The token was not generated by the server)
        throw new IllegalStateException("token was not generated by the server");
    }

    private boolean isExpired(LocalDateTime expiredAt) {
        LocalDateTime now = clock.now();
        return now.isAfter(expiredAt);
    }

    private void confirmToken(ConfirmationToken confirmationToken) {
        confirmationToken.confirm();
    }
}
