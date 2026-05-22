package com.uees.studentservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class StudentServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentServicesApplication.class, args);
    }
}
