package com.intolighter.appealssystem.web.errors;

import com.intolighter.appealssystem.web.errors.exceptions.AppealAlreadyExistsException;
import com.intolighter.appealssystem.web.errors.exceptions.AppealNotFoundException;
import com.intolighter.appealssystem.web.errors.exceptions.UserAlreadyExistsException;
import com.intolighter.appealssystem.web.errors.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class RootControllerAdvice {

    @ExceptionHandler({AppealNotFoundException.class, UserNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage domainNotFound(Exception e, WebRequest request) {
        return new ErrorMessage(
                HttpStatus.NOT_FOUND,
                e,
                request.getDescription(false));
    }

    @ExceptionHandler({UserAlreadyExistsException.class, AppealAlreadyExistsException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessage alreadyExists(Exception e, HttpServletRequest request) {
        return new ErrorMessage(
                HttpStatus.FORBIDDEN,
                e,
                request.getRequestURI());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage argumentNotValid(MethodArgumentNotValidException e, WebRequest request) {
        return new ErrorMessage(
                HttpStatus.BAD_REQUEST,
                e,
                request.getDescription(false));
    }
}
