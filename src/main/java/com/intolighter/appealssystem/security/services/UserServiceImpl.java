package com.intolighter.appealssystem.security.services;

import com.google.common.collect.Sets;
import com.intolighter.appealssystem.errors.exceptions.UserAlreadyExistsException;
import com.intolighter.appealssystem.errors.exceptions.UserNotFoundException;
import com.intolighter.appealssystem.models.ERole;
import com.intolighter.appealssystem.models.User;
import com.intolighter.appealssystem.models.VerificationToken;
import com.intolighter.appealssystem.repositories.UserRepository;
import com.intolighter.appealssystem.repositories.VerificationTokenRepository;
import com.intolighter.appealssystem.requests.SignInRequest;
import com.intolighter.appealssystem.requests.SignUpRequest;
import com.intolighter.appealssystem.responses.JwtResponse;
import com.intolighter.appealssystem.security.jwt.JwtUtils;
import lombok.val;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private VerificationTokenRepository tokenRepository;
    private PasswordEncoder encoder;
    private AuthenticationManager authenticationManager;
    private JwtUtils jwtUtils;

    public UserServiceImpl(UserRepository userRepository, VerificationTokenRepository tokenRepository, PasswordEncoder encoder, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Override
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

        val userRoles = Sets.newHashSet(ERole.ROLE_USER);
        user.setRoles(userRoles);

        return userRepository.save(user);
    }

    @Override
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

    @Override
    public void setEnabled(User user) {
        userRepository.updateEnabled(user);
    }

    @Override
    public void createVerificationToken(User user, String token) {
        tokenRepository.save(new VerificationToken(token, user));

    }
}
