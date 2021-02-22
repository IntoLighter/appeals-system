package com.intolighter.appealssystem.controllers;

import com.intolighter.appealssystem.events.OnRegistrationCompleteEvent;
import com.intolighter.appealssystem.repositories.VerificationTokenRepository;
import com.intolighter.appealssystem.requests.SignInRequest;
import com.intolighter.appealssystem.requests.SignUpRequest;
import com.intolighter.appealssystem.responses.MessageResponse;
import com.intolighter.appealssystem.security.services.UserService;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.Valid;
import java.util.Calendar;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ApplicationEventPublisher eventPublisher;
    private final VerificationTokenRepository tokenRepository;
    private final MessageSource messages;
    private final UserService service;

    public AuthController(
            ApplicationEventPublisher eventPublisher,
            VerificationTokenRepository tokenRepository,
            @Qualifier("messagesSource") MessageSource messages,
            UserService service) {
        this.eventPublisher = eventPublisher;
        this.tokenRepository = tokenRepository;
        this.messages = messages;
        this.service = service;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest signUpRequest, WebRequest request) {
        val appUrl = request.getContextPath();
        val user = service.registerNewUserAccount(signUpRequest);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, request.getLocale(), appUrl));

        return ResponseEntity.ok(new MessageResponse("User created successfully"));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> singIn(@Valid @RequestBody SignInRequest signInRequest) {
        return ResponseEntity.ok(service.loginUser(signInRequest));
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirmRegistration(@RequestParam("token") String token, WebRequest request) {
        val locale = request.getLocale();

        val verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null) {
            String message = messages.getMessage("auth.message.invalidToken", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }

        val user = verificationToken.getUser();
        val cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            val message = messages.getMessage("auth.message.expired", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }

        service.setEnabled(user);
        return ResponseEntity.ok(new MessageResponse("User confirmed successfully"));
    }
}
