package com.projectmanagementsaas.auth.security;

import com.projectmanagementsaas.user.entity.User;
import java.util.Collection;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record AuthenticatedUser(User user, Collection<? extends GrantedAuthority> authorities) implements UserDetails {
    public UUID id() {
        return user.getId();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
