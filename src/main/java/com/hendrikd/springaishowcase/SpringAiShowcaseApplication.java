package com.hendrikd.springaishowcase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringAiShowcaseApplication {

    public static void main(String[] args) {
        System.out.println(">>> [APP] Starting Application ...");
        SpringApplication.run(SpringAiShowcaseApplication.class, args);
        System.out.println(">>> [APP] Application started! Use GET Request to /describe endpoint.");
    }
}
