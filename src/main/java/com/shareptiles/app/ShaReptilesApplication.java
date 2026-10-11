package com.shareptiles.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class ShaReptilesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShaReptilesApplication.class, args);
    }
}
