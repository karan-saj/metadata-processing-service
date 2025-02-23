package com.lily.metadataProcessingService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Metadata Processing Service.
 * This service handles metadata from different sources (like Monte Carlo, Slack)
 * and processes them based on rules.
 */
@Slf4j
@SpringBootApplication
public class MetadataProcessingServiceApplication {

	public static void main(String[] args) {
		log.info("Initalizing Metadata Processing Service");
		SpringApplication.run(MetadataProcessingServiceApplication.class, args);
		log.info("Metadata Processing Service is now running");
	}

}
