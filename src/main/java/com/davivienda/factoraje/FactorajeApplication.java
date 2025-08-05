package com.davivienda.factoraje;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FactorajeApplication {

	public static void main(String[] args) {
		SpringApplication.run(FactorajeApplication.class, args);
	}
}
