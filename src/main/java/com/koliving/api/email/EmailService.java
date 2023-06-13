package com.koliving.api.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


@Service
@Slf4j
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;
    private final MessageSource messageSource;
    private final EmailTemplateUtil emailTemplateUtil;
    private String mailHost;

    public EmailService(JavaMailSender mailSender, MessageSource messageSource, EmailTemplateUtil emailTemplateUtil, @Value("${spring.mail.username}") String mailHost) {
        this.mailSender = mailSender;
        this.messageSource = messageSource;
        this.emailTemplateUtil = emailTemplateUtil;
        this.mailHost = mailHost;
    }

    @Async
    @Override
    public void send(MailType type, String to, String link) {
        try {
            Locale currentLocale = LocaleContextHolder.getLocale();

            String title = "KOLIVING";
            String subtitle = messageSource.getMessage("auth_email_subtitle", null, currentLocale);
            String buttonBackgroundColor = "#FF8E00";
            String buttonImageSrc = "image/email-logo.svg";

            Map<String, Object> variables = new HashMap<>();
            variables.put("title", title);
            variables.put("subtitle", subtitle);
            variables.put("auth-email-link", link);
            variables.put("button-background-color", buttonBackgroundColor);
            variables.put("button-image-src", buttonImageSrc);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            String mailContent = emailTemplateUtil.generateEmail(type, variables);
            String subject = messageSource.getMessage("auth_email_subject", null, currentLocale);

            helper.setSubject(subject);
            helper.setTo(to);
            helper.setText(mailContent, true);
            helper.setFrom(mailHost);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("failed to send email", e);
            throw new IllegalStateException("failed to send email");
        }
    }
}