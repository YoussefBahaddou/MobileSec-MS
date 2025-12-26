package com.mobilesec.fixsuggest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class FixSuggestApplication {

    public static void main(String[] args) {
        SpringApplication.run(FixSuggestApplication.class, args);
    }

}
