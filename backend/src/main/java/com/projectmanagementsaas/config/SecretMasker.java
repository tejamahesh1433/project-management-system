package com.projectmanagementsaas.config;

import org.springframework.stereotype.Component;

@Component
public class SecretMasker {
    public String mask(String value) {
        if (value == null || value.isBlank()) {
            return "<missing>";
        }
        if (value.length() <= 8) {
            return "********";
        }
        return value.substring(0, 4) + "..." + value.substring(value.length() - 4);
    }
}
