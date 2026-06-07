package com.projectmanagementsaas.security;

import com.projectmanagementsaas.common.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class PasswordPolicyValidator {
    public void validate(String password) {
        if (password == null || password.length() < 12 || password.length() > 128) {
            throw new BadRequestException("Password must be between 12 and 128 characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new BadRequestException("Password must include an uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new BadRequestException("Password must include a lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new BadRequestException("Password must include a number");
        }
        if (!password.matches(".*[^A-Za-z0-9].*")) {
            throw new BadRequestException("Password must include a special character");
        }
    }
}
