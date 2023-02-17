package com.side.fitflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class FitflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitflowApplication.class, args);
    }

}
