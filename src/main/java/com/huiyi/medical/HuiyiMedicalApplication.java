package com.huiyi.medical;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.huiyi.medical.mapper")
public class HuiyiMedicalApplication {
    public static void main(String[] args) {
        SpringApplication.run(HuiyiMedicalApplication.class, args);
    }
}
