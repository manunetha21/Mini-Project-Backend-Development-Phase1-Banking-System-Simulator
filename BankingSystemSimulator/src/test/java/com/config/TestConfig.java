package com.config;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.service.AccountService;

    @Configuration
    public class TestConfig {

        @Bean
        public AccountService accountService() {

            return Mockito.mock(AccountService.class);
        }
    }

