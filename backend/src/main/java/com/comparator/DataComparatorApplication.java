package com.comparator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DataComparatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataComparatorApplication.class, args);
    }
}
