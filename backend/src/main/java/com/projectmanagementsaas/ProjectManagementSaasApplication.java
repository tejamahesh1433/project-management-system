package com.projectmanagementsaas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProjectManagementSaasApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProjectManagementSaasApplication.class, args);
    }
}
