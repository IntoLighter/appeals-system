package com.intolighter.appealssystem.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        val mapper = new ObjectMapper();
        val cookie = new Cookie("cookich", "fukich");
        response.addCookie(cookie);
        response.sendError(HttpServletResponse.SC_CONTINUE, "Called from AuthTokenDeniedHandler");
    }
}
