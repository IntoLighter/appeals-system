package com.intolighter.appealssystem.persistence.models;

import lombok.val;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class TokenDtoUtils {

    public static Date calculateExpiryDate() {
        val cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, 2000);
        return new Date(cal.getTime().getTime());
    }
}
