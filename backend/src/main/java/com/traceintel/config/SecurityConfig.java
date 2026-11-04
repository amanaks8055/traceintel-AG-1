package com.traceintel.config;

import com.traceintel.security.JwtAuthenticationFilter;
import com.traceintel.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService userDetailsService
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/vin/links").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/trace/history").hasAnyRole("ADMIN", "QUALITY_ENGINEER", "MANAGER")
                        .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/inspections/**").hasAnyRole("ADMIN", "SUPERVISOR", "QUALITY_ENGINEER", "MANAGER")
                        .requestMatchers("/api/lots/**", "/api/batches/**", "/api/lookups/**", "/api/qr/**")
                        .hasAnyRole("ADMIN", "OPERATOR", "SUPERVISOR", "QUALITY_ENGINEER", "MANAGER")
                        .requestMatchers("/api/scan/**").hasAnyRole("ADMIN", "OPERATOR", "SUPERVISOR")
                        .requestMatchers("/api/trace/**").hasAnyRole("ADMIN", "SUPERVISOR", "QUALITY_ENGINEER", "MANAGER")
                        .requestMatchers("/api/holds/**").hasAnyRole("ADMIN", "SUPERVISOR", "QUALITY_ENGINEER", "MANAGER")
                        .requestMatchers("/api/shipments/**").hasAnyRole("ADMIN", "OPERATOR", "SUPERVISOR")
                        .requestMatchers("/api/audit/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/dashboard/**").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(errors -> errors.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") String origins,
            @Value("${app.cors.allowed-methods}") String methods,
            @Value("${app.cors.allowed-headers}") String headers,
            @Value("${app.cors.allow-credentials}") Boolean allowCredentials
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        // Values pasted into a hosting dashboard often carry spaces, a new line or a trailing slash.
        configuration.setAllowedOrigins(Arrays.stream(origins.split(","))
                .map(String::strip)
                .map(origin -> origin.endsWith("/") ? origin.substring(0, origin.length() - 1) : origin)
                .filter(origin -> !origin.isEmpty())
                .toList());
        configuration.setAllowedMethods(Arrays.asList(methods.split(",")));
        configuration.setAllowedHeaders(Arrays.asList(headers.split(",")));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
