package outbroker_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OutbrokerBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OutbrokerBackendApplication.class, args);
    }
}