package outbroker_backend.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI outBrokerOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("OutBroker REST API")
                        .description(
                                "OutBroker property marketplace backend API for " +
                                "property listings, search, verification, inquiries, " +
                                "notifications, reports, owner workflows, and admin operations."
                        )
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("OutBroker Engineering Team")))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(
                        new SecurityRequirement().addList("bearerAuth")
                );
    }
}