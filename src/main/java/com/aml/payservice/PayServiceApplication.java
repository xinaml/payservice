package com.aml.payservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PayServiceApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(PayServiceApplication.class, args);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
