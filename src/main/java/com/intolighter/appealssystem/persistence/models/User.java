package com.intolighter.appealssystem.persistence.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"phoneNumber", "email"})})
@JsonIgnoreProperties({"id", "phoneNumber", "email", "password", "role", "enabled"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String password;
    private ERole role;
    @Column(name = "enabled")
    private boolean enabled = false;

    @OneToMany(mappedBy = "user")
    private List<Appeal> appeals;

    public User(String firstName, String lastName, String email, String password,
                String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    public User(long id, String firstName, String phoneNumber, String email,
                String password, boolean enabled, ERole role) {
        this.id = id;
        this.firstName = firstName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.role = role;
    }
}
