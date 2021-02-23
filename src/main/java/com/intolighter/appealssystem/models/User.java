package com.intolighter.appealssystem.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
//        uniqueConstraints = {@UniqueConstraint(columnNames = {"phoneNumber", "email"})})
@JsonIgnoreProperties({"id", "phoneNumber", "email", "password", "roles", "enabled"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String password;
    @ElementCollection
    private Set<ERole> roles = new HashSet<>();
    @Column(name = "enabled")
    private boolean enabled = false;

    public User(String firstName, String lastName, String email, String password, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    public User(long id, String firstName, String phoneNumber, String email, String password, boolean enabled) {
        this.id = id;
        this.firstName = firstName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
    }
}
