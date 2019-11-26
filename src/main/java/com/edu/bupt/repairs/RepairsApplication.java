package com.edu.bupt.repairs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.edu.bupt.repairs.dao")
public class RepairsApplication {
    public static void main(String[] args) {
        SpringApplication.run(RepairsApplication.class, args);
    }
}
