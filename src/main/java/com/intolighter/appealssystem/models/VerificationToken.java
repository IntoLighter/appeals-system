package com.intolighter.appealssystem.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class VerificationToken {
    private static final int EXPIRATION = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;

    //    @JoinColumn(nullable = false, name = "user_id")
    @OneToOne
    private User user;

    private Date expiryDate;

    public VerificationToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate();
    }

    private Date calculateExpiryDate() {
        val cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, 2000);
        return new Date(cal.getTime().getTime());
    }
}
