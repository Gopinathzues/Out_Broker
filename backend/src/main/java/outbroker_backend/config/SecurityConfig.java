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
        // Authentication is handled through the custom JWT filter.
        return username -> {
            throw new UsernameNotFoundException(
                    "User authentication is handled via JWT tokens."
            );
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Stateless JWT security
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // =====================================================
                        // AUTHENTICATION
                        // =====================================================
                        .requestMatchers("/api/v1/auth/**")
                        .permitAll()

                        // =====================================================
                        // SWAGGER / OPENAPI
                        // =====================================================
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        )
                        .permitAll()

                        // =====================================================
                        // PUBLIC PROPERTY DISCOVERY
                        // =====================================================
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/properties"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/properties/search"
                        )
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/properties/search/v2").permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/properties/filter"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/properties/nearby"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/properties/*"
                        )
                        .permitAll()

                        // =====================================================
                        // PROPERTY IMAGES
                        // =====================================================
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/properties/*/images/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/properties/*/images/**"
                        )
                        .hasAnyRole("LANDLORD", "BROKER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/properties/*/images/**"
                        )
                        .hasAnyRole("LANDLORD", "BROKER", "ADMIN")

                        // =====================================================
                        // INQUIRIES
                        // =====================================================
                        .requestMatchers("/api/v1/inquiries/owner")
                        .hasAnyRole("LANDLORD", "BROKER", "ADMIN")

                        .requestMatchers("/api/v1/inquiries/**")
                        .authenticated()

                        // =====================================================
                        // PROPERTY VERIFICATION
                        // =====================================================

                        // Property owner submits verification
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/properties/*/verify"
                        )
                        .hasAnyRole("LANDLORD", "ADMIN")

                        // Admin reviews verification
                        .requestMatchers(
                                "/api/admin/**"
                        )
                        .hasRole("ADMIN")

                        // =====================================================
                        // ADMIN APIs
                        // =====================================================
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")

                        // =====================================================
                        // OWNER APIs
                        // =====================================================
                        .requestMatchers("/api/v1/owner/**")
                        .hasAnyRole("LANDLORD", "ADMIN")

                        // =====================================================
                        // REPORTS
                        // =====================================================

                        // Anyone authenticated can submit a report
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/reports"
                        )
                        .authenticated()

                        // Only admins can view reports
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/reports/**"
                        )
                        .hasRole("ADMIN")

                        // Only admins can change report status
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/reports/**"
                        )
                        .hasRole("ADMIN")

                        // =====================================================
                        // STATIC UPLOADS
                        // =====================================================
                        .requestMatchers("/uploads/**")
                        .permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        // =====================================================
                        // EVERYTHING ELSE
                        // =====================================================
                        .anyRequest()
                        .authenticated()
                )

                // =============================================================
                // JWT FILTER
                // =============================================================
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}