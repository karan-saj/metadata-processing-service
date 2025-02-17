package com.lily.metadataProcessingService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class MetadataProcessingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetadataProcessingServiceApplication.class, args);
		log.info("Application started");
	}

}
