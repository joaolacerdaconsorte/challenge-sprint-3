package br.com.fiap.pet360;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Pet360Application {

    public static void main(String[] args) {
        SpringApplication.run(Pet360Application.class, args);
    }
}
