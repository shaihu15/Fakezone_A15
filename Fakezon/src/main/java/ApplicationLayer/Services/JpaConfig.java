package ApplicationLayer.Services;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "InfrastructureLayer")
@EntityScan(basePackages = "DomainLayer.Model")
public class JpaConfig {
    // Configuration for JPA repositories and entity scanning
} 