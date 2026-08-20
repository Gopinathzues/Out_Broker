package outbroker_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/properties/filter").permitAll()

                        // Owner-specific endpoint - requires LANDLORD role
                        .requestMatchers("/api/v1/inquiries/owner").hasRole("LANDLORD")

                        // All other inquiries endpoints - just authenticated
                        .requestMatchers("/api/v1/inquiries/**").authenticated()

                        // Property Images security rules
                        .requestMatchers(HttpMethod.GET, "/api/v1/properties/*/images/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/properties/*/images/**").hasAnyRole("LANDLORD", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/properties/*/images/**").hasAnyRole("LANDLORD", "ADMIN")

                        // Static Uploads
                        .requestMatchers("/uploads/**").permitAll()

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}