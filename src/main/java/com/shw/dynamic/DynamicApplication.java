package com.shw.dynamic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author shw
 * @version 1.0
 * @date 2020/9/12 12:39
 * @description
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class DynamicApplication {

    public static void main(String[] args) {

        SpringApplication.run(DynamicApplication.class,args);

    }

}
