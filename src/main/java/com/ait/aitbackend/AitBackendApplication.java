package com.ait.aitbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AitBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AitBackendApplication.class, args);
    }

}
