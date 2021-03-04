package com.intolighter.appealssystem.persistence.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Date;

import static com.intolighter.appealssystem.persistence.models.TokenDtoUtils.calculateExpiryDate;

@Entity
@Data
@NoArgsConstructor
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String token;

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    private Date expiryDate = calculateExpiryDate();

    public VerificationToken(String token, User user) {
        this.token = token;
        this.user = user;
    }

}
