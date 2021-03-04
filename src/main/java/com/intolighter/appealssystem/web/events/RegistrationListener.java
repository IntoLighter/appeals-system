package com.intolighter.appealssystem.web.events;

import com.intolighter.appealssystem.services.UserService;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.UUID;

@SuppressWarnings("NullableProblems")
@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final MessageSource messageSource;
    private final UserService userService;

    public RegistrationListener(
            @Qualifier("messagesSource") MessageSource messageSource,
            UserService userService) {
        this.messageSource = messageSource;
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        val user = event.getUser();
        val token = UUID.randomUUID().toString();
        userService.createVerificationToken(user, token);

        val confirmationUrl = event.getAppUrl() + "/auth/confirm/" + token;
        val message = messageSource.getMessage("message.regSucc", null, event.getLocale());
        val body = message + "\r\n" + "http://localhost:9010" + confirmationUrl;
        userService.sendEmail("Registration Confirmation", body, user);
    }
}
