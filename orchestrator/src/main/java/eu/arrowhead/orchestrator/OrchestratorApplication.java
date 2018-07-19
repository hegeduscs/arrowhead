package eu.arrowhead.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("eu.arrowhead")
@EntityScan("eu.arrowhead.common.dto.entity")
@SpringBootApplication(scanBasePackages = "eu.arrowhead")
public class OrchestratorApplication {

  public static void main(String[] args) {
    SpringApplication.run(OrchestratorApplication.class, args);
  }

}
