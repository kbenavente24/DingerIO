package com.kobe.dinger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DingerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DingerApplication.class, args);
	}

}
