package com.intolighter.appealssystem.security.services;

import com.intolighter.appealssystem.models.User;
import com.intolighter.appealssystem.requests.SignInRequest;
import com.intolighter.appealssystem.requests.SignUpRequest;
import com.intolighter.appealssystem.responses.JwtResponse;

public interface UserService {

    User registerNewUserAccount(SignUpRequest signUpRequest);

    JwtResponse loginUser(SignInRequest signInRequest);

    void createVerificationToken(User user, String token);

    void setEnabled(User user);
}
