package com.intolighter.appealssystem.persistence.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Date;

import static com.intolighter.appealssystem.persistence.models.TokenDtoUtils.calculateExpiryDate;

@Entity
@NoArgsConstructor
@Data
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String token;

    @OneToOne(targetEntity = User.class)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    private Date expiryDate = calculateExpiryDate();

    public PasswordResetToken(String token, User user) {
        this.token = token;
        this.user = user;
    }
}
