package com.intolighter.appealssystem.web.models.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
public class SignInRequest {
    @Size(max = 50)
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 4, max = 40)
    private String password;
}
