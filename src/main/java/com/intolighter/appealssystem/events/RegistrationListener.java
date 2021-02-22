package com.intolighter.appealssystem.events;

import com.intolighter.appealssystem.security.services.UserService;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.UUID;

@SuppressWarnings("NullableProblems")
@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final MessageSource messageSource;
    private final JavaMailSender mailSender;
    private final UserService service;

    public RegistrationListener(
            @Qualifier("messagesSource") MessageSource messageSource,
            JavaMailSender mailSender,
            UserService service) {
        this.messageSource = messageSource;
        this.mailSender = mailSender;
        this.service = service;
    }

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        val user = event.getUser();
        val token = UUID.randomUUID().toString();
        service.createVerificationToken(user, token);

        val recipientAddress = user.getEmail();
        System.out.println(event.getAppUrl());
        val confirmationUrl = event.getAppUrl() + "/auth/confirm?token=" + token;
        val message = messageSource.getMessage("message.regSucc", null, event.getLocale());

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject("Registration Confirmation");
        email.setText(message + "\r\n" + "http://localhost:9010" + confirmationUrl);
        mailSender.send(email);
    }
}
