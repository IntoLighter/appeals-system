package com.intolighter.appealssystem.persistence.models;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PasswordDto {

    @NotBlank
    private  String token;

    @NotBlank
    private String password;
}
