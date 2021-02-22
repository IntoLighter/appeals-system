package com.intolighter.appealssystem.errors;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Date;

@Getter
public class ErrorMessage {
    private int statusCode;
    private Date timestamp = new Date();
    private String message;
    private String path;

    public ErrorMessage(HttpStatus status, Exception exception, String path) {
        this.message = exception.getMessage();
        this.path = path;
        this.statusCode = status.value();
    }
}
