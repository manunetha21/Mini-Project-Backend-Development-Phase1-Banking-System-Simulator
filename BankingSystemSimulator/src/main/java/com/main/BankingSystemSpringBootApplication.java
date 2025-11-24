package com.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "com")
@EnableMongoRepositories("com.repository")
public class BankingSystemSpringBootApplication {

	public static void main(String[] args) {

        SpringApplication.run(BankingSystemSpringBootApplication.class, args);
        System.out.println("Banking System SpringBoot Application started");

	}

}
