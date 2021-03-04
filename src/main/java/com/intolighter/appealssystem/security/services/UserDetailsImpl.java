package com.intolighter.appealssystem.security.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intolighter.appealssystem.persistence.models.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@AllArgsConstructor
@Getter
public class UserDetailsImpl implements UserDetails {
    @Getter(AccessLevel.NONE)
    private final Collection<? extends GrantedAuthority> authorities;
    private long id;
    private String firstName;
    private String lastName;
    private String email;
    @JsonIgnore
    private String password;
    private String phoneNumber;
    private boolean enabled;

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));

        return new UserDetailsImpl(
                authorities,
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPassword(),
                user.getPhoneNumber(),
                user.isEnabled());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return Objects.equals(id, ((UserDetailsImpl) o).id);
    }
}
