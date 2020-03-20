package fridge;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableConfigurationProperties
@EnableWebFlux
@EnableReactiveMongoRepositories
public class FridgeApplication implements CommandLineRunner {
	public static void main(String[] args) {
		SpringApplication.run(FridgeApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Nothing for now
	}
}
