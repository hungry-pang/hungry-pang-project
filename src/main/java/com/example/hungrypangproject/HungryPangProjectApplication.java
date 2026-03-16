package com.example.hungrypangproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class HungryPangProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(HungryPangProjectApplication.class, args);
    }

}
