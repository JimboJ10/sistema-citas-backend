package com.jordyjimbo.sistema_citas;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SistemaCitasApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemaCitasApplication.class, args);
	}

	@Bean
	public CommandLineRunner diagnostico(ApplicationContext context) {
		return args -> {
			System.out.println("=== DIAGNOSTICO SECURITY ===");
			String[] beans = context.getBeanNamesForType(org.springframework.security.web.SecurityFilterChain.class);
			System.out.println("Beans SecurityFilterChain encontrados: " + beans.length);
			for (String bean : beans) {
				System.out.println(" -> " + bean);
			}
			System.out.println("=============================");
		};
	}
}