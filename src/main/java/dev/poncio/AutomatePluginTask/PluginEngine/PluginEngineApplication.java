package dev.poncio.AutomatePluginTask.PluginEngine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class PluginEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(PluginEngineApplication.class, args);
	}

}
