package com.pdfcompare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("Application lancée ! Ouvrez http://localhost:8080 dans votre navigateur.");
    }
}