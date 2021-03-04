package com.intolighter.appealssystem.web.controllers;

import com.intolighter.appealssystem.web.events.OnRegistrationCompleteEvent;
import com.intolighter.appealssystem.persistence.models.PasswordDto;
import com.intolighter.appealssystem.persistence.repositories.VerificationTokenRepository;
import com.intolighter.appealssystem.services.UserService;
import com.intolighter.appealssystem.web.models.requests.SignInRequest;
import com.intolighter.appealssystem.web.models.requests.SignUpRequest;
import com.intolighter.appealssystem.web.models.responses.MessageResponse;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Calendar;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ApplicationEventPublisher eventPublisher;
    private final VerificationTokenRepository tokenRepository;
    private final MessageSource messages;
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(ApplicationEventPublisher eventPublisher,
                          VerificationTokenRepository tokenRepository,
                          @Qualifier("messagesSource") MessageSource messages,
                          UserService userService) {
        this.eventPublisher = eventPublisher;
        this.tokenRepository = tokenRepository;
        this.messages = messages;
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest signUpRequest, HttpServletRequest request) {
        val appUrl = request.getContextPath();
        val user = userService.registerNewUserAccount(signUpRequest);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, request.getLocale(), appUrl));

        return ResponseEntity.ok(new MessageResponse("User created successfully"));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> singIn(@Valid @RequestBody SignInRequest signInRequest) {
        return ResponseEntity.ok(userService.loginUser(signInRequest));
    }

    @GetMapping("/confirm/{token}")
    public ResponseEntity<?> confirmRegistration(@PathVariable String token,
                                                 HttpServletRequest request) {
        val locale = request.getLocale();

        val verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null) {
            val message = messages.getMessage("auth.message.invalidToken", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }

        val user = verificationToken.getUser();
        val cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            val message = messages.getMessage("auth.message.expired", null, locale);
            userService.deleteVerificationToken(verificationToken);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }

        userService.setUserEnabled(user);
        userService.deleteVerificationToken(verificationToken);
        return ResponseEntity.ok(new MessageResponse("User confirmed successfully"));
    }

    @PostMapping("/resetPassword/{email}")
    public ResponseEntity<?> resetPassword(HttpServletRequest request,
                                           @PathVariable String email) {
        val user = userService.findUserByEmail(email);
        val token = UUID.randomUUID().toString();
        userService.createPasswordResetTokenForUser(user, token);

        val appUrl = "http://" + request.getServerName() + ":"
                + request.getServerPort() + request.getContextPath();

        val url = appUrl + "/auth/changePassword?token=" + token;
        val message = messages.getMessage("message.resetPassword", null, request.getLocale());
        userService.sendEmail("Reset Password", message + " \r\n" + url, user);

        return ResponseEntity.ok(new MessageResponse(messages.getMessage(
                "message.resetPasswordEmail", null, request.getLocale())));
    }

    @GetMapping("/changePassword/{token}")
    public ResponseEntity<?> changePassword(HttpServletRequest request,
                                            @PathVariable String token) {
        val result = userService.validatePasswordResetToken(token);
        if (result != null) {
            val message = messages.getMessage("auth.message." + result,
                    null, request.getLocale());
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        } else {
            return ResponseEntity.ok(new MessageResponse("Token is correct"));
        }
    }

    @PostMapping("/savePassword")
    public ResponseEntity<?> savePassword(HttpServletRequest request,
                                          @Valid @RequestBody PasswordDto passwordDto) {
        val locale = request.getLocale();
        val token = passwordDto.getToken();
        val result = userService.validatePasswordResetToken(token);

        if (result != null) {
            return ResponseEntity.badRequest().body(new MessageResponse(messages.getMessage(
                    "auth.message." + result, null, locale)));
        }

        val user = userService.findUserByPasswordResetToken(token);
        userService.changeUserPassword(user, passwordDto.getPassword());
        userService.deletePasswordResetToken(token);
        return ResponseEntity.ok(new MessageResponse(messages.getMessage(
                "message.resetPasswordSuc", null, locale)));
    }
}
