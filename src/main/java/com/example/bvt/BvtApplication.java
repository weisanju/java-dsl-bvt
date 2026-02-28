package com.example.bvt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BvtApplication {

    public static void main(String[] args) {
        SpringApplication.run(BvtApplication.class, args);
    }
}
