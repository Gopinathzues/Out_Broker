package outbroker_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    public UserDetailsService userDetailsService() {
        // Disables default Spring Security in-memory user generation
        return username -> {
            throw new UsernameNotFoundException("User authentication is handled via JWT tokens.");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Authentication
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Public property discovery
                        .requestMatchers(HttpMethod.GET, "/api/v1/properties").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/properties/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/properties/filter").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/properties/nearby").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/properties/*").permitAll()

                        // Owner inquiry endpoint
                        .requestMatchers("/api/v1/inquiries/owner").hasRole("LANDLORD")

                        // Other inquiry endpoints
                        .requestMatchers("/api/v1/inquiries/**").authenticated()

                        // Property images
                        .requestMatchers(HttpMethod.GET, "/api/v1/properties/*/images/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/properties/*/images/**")
                            .hasAnyRole("LANDLORD", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/properties/*/images/**")
                            .hasAnyRole("LANDLORD", "ADMIN")

                        // Static uploads
                        .requestMatchers("/uploads/**").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}