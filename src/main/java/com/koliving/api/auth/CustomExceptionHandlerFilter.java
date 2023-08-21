package com.koliving.api.auth;

import com.koliving.api.dto.ValidationResult;
import com.koliving.api.exception.BlackListTokenException;
import com.koliving.api.exception.LoginInvalidException;
import com.koliving.api.utils.HttpUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.util.Locale;

@RequiredArgsConstructor
public class CustomExceptionHandlerFilter extends OncePerRequestFilter {

    private final HttpUtils httpUtils;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Locale locale = localeResolver.resolveLocale(request);

        try {
            filterChain.doFilter(request, response);
        } catch (LoginInvalidException e) {
            ValidationResult errors = ValidationResult.of(e.getErrors(), messageSource, locale);

            httpUtils.setResponse(
                    response,
                    httpUtils.createFailureResponse(errors, HttpServletResponse.SC_BAD_REQUEST)
            );
        } catch (AuthenticationException | JwtException e) {
            String errorMessage = messageSource.getMessage(e.getMessage(), null, locale);

            httpUtils.setResponseWithRedirect(
                    response,
                    httpUtils.createFailureResponse(errorMessage, HttpServletResponse.SC_UNAUTHORIZED),
                    "/api/login"
            );
        } catch (BlackListTokenException e) {
            String errorMessage = messageSource.getMessage(e.getMessage(), null, locale);

            httpUtils.setResponseWithRedirect(
                    response,
                    httpUtils.createFailureResponse(errorMessage, HttpServletResponse.SC_UNAUTHORIZED),
                    "/api/login"
            );
        } catch (RuntimeException e) {
            httpUtils.setResponse(
                    response,
                    httpUtils.createFailureResponse("mapping error", HttpServletResponse.SC_BAD_REQUEST)
            );
        }
    }
}
