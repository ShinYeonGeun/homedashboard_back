package com.lotus.homeDashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.lotus.homeDashboard")
public class HomedashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomedashboardApplication.class, args);
	}

}
