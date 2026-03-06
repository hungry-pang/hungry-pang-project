package com.example.hungrypangproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class HungryPangProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(HungryPangProjectApplication.class, args);
    }

}
