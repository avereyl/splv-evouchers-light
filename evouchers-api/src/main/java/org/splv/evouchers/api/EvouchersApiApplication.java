package org.splv.evouchers.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.splv.evouchers")
public class EvouchersApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EvouchersApiApplication.class, args);
	}

}
