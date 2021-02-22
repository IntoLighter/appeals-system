package com.intolighter.appealssystem.responses;

import com.intolighter.appealssystem.security.services.UserDetailsImpl;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private long id;
    private String username;
    private String email;
    private String phoneNumber;
    private List<String> roles;

    public JwtResponse(String token, UserDetailsImpl userDetails, List<String> roles) {
        this.token = token;
        this.id = userDetails.getId();
        this.username = userDetails.getUsername();
        this.email = userDetails.getEmail();
        this.phoneNumber = userDetails.getPhoneNumber();
        this.roles = roles;
    }
}
