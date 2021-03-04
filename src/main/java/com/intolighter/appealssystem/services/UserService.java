package com.intolighter.appealssystem.services;

import com.intolighter.appealssystem.web.errors.exceptions.UserAlreadyExistsException;
import com.intolighter.appealssystem.web.errors.exceptions.UserNotFoundException;
import com.intolighter.appealssystem.persistence.models.ERole;
import com.intolighter.appealssystem.persistence.models.PasswordResetToken;
import com.intolighter.appealssystem.persistence.models.User;
import com.intolighter.appealssystem.persistence.models.VerificationToken;
import com.intolighter.appealssystem.persistence.repositories.PasswordResetRepository;
import com.intolighter.appealssystem.persistence.repositories.UserRepository;
import com.intolighter.appealssystem.persistence.repositories.VerificationTokenRepository;
import com.intolighter.appealssystem.security.jwt.JwtUtils;
import com.intolighter.appealssystem.security.services.UserDetailsImpl;
import com.intolighter.appealssystem.web.models.requests.*;
import com.intolighter.appealssystem.web.models.responses.JwtResponse;
import lombok.val;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordResetRepository passwordResetRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       VerificationTokenRepository tokenRepository,
                       PasswordEncoder encoder,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils,
                       PasswordResetRepository passwordResetRepository,
                       JavaMailSender mailSender,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.passwordResetRepository = passwordResetRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerNewUserAccount(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new UserAlreadyExistsException(
                    "There is an account with the email: " + signUpRequest.getEmail());
        }

        if (userRepository.existsByPhoneNumber(signUpRequest.getPhoneNumber())) {
            throw new UserAlreadyExistsException(
                    "There is an account with the phoneNumber: " + signUpRequest.getPhoneNumber());
        }

        val user = new User(
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getPhoneNumber());
        user.setRole(ERole.ROLE_USER);

        return userRepository.save(user);
    }

    public JwtResponse loginUser(SignInRequest signInRequest) {
        if (!userRepository.existsByEmail(signInRequest.getEmail())) {
            throw new UserNotFoundException(
                    "User with email '" + signInRequest.getEmail() + "' not found");
        }

        val authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInRequest.getEmail(), signInRequest.getPassword()));
        val userDetails = (UserDetailsImpl) authentication.getPrincipal();

        SecurityContextHolder.getContext().setAuthentication(authentication);
        val jwt = jwtUtils.generateJwtToken(authentication);

        val roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new JwtResponse(jwt, userDetails, roles);
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new UserNotFoundException("User with email " + email + "is not found"));
    }

    public User findUserByPasswordResetToken(String token) {
        return passwordResetRepository.findByToken(token).getUser();
    }

    public void createPasswordResetTokenForUser(User user, String token) {
        passwordResetRepository.save(new PasswordResetToken(token, user));
    }

    public void setUserEnabled(User user) {
        userRepository.updateEnabled(user);

    }

    public void createVerificationToken(User user, String token) {
        tokenRepository.save(new VerificationToken(token, user));
    }

    public void deleteVerificationToken(VerificationToken token) {
        tokenRepository.delete(token);
    }

    public void deletePasswordResetToken(String token) {
        passwordResetRepository.delete(passwordResetRepository.findByToken(token));
    }

    public void sendEmail(String subject, String body, User user) {
        val email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
        mailSender.send(email);
    }

    public void changeUserPassword(User user, String password) {
        userRepository.updatePassword(passwordEncoder.encode(password), user);
    }

    public String validatePasswordResetToken(String token) {
        val passToken = passwordResetRepository.findByToken(token);

        return passToken == null ? "invalidToken"
                : isTokenExpired(passToken) ? "expired"
                : null;
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        val cal = Calendar.getInstance();
        return passToken.getExpiryDate().before(cal.getTime());
    }
}
