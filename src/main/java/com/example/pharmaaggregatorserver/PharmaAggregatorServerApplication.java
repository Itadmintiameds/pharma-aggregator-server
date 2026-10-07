package com.example.pharmaaggregatorserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
//@EnableAsync
public class PharmaAggregatorServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmaAggregatorServerApplication.class, args);
    }

}
