package com.intolighter.appealssystem.web.errors.exceptions;

public class AppealNotFoundException extends RuntimeException {

    public AppealNotFoundException(long id) {
        super("Could not find appeal with id: " + id);
    }
}
