package com.example.springbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class SpringBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBackendApplication.class, args);
        System.out.println("test");
    }
}
