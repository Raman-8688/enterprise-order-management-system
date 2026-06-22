package com.enterprise.oms.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
public class ConfigServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
		System.out.println("=".repeat(50));
		System.out.println("Config Server Started Successfully!");
		System.out.println("Port: http://localhost:8888");
		System.out.println("Eureka: Registered as CONFIG-SERVER");
		System.out.println("=".repeat(50));
	}
}