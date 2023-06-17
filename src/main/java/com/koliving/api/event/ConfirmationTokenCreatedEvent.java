package com.koliving.api.event;

import com.koliving.api.token.ConfirmationToken;
import lombok.Getter;

@Getter
public class ConfirmationTokenCreatedEvent  {

    private final String email;
    private final String token;

    public ConfirmationTokenCreatedEvent(ConfirmationToken savedToken) {
        this.email = savedToken.getEmail();
        this.token = savedToken.getToken();
    }
}
