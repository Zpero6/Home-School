package com.zpero;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zpero.mapper")
public class HomeSchoolApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeSchoolApplication.class, args);
    }
}
