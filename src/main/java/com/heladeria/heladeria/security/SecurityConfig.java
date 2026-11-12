package com.heladeria.heladeria.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 🔑 Define qué rutas están protegidas y cuáles no.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors() // habilita CORS usando tu CorsConfigurationSource
                .and()
                .csrf(csrf -> csrf.disable()) // Deshabilita CSRF para APIs REST
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        //login
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register").hasRole("ADMIN_GLOBAL")
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/users", "/api/users/**")
                        .hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        .requestMatchers(HttpMethod.GET, "/api/branches").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN_GLOBAL")
                        .requestMatchers("/api/dashboard/**").hasRole("ADMIN_GLOBAL")
                        .requestMatchers("/api/purchase-orders/**").hasRole("ADMIN_GLOBAL")
                        .requestMatchers(HttpMethod.GET, "/helados/sale", "/helados/sale/{id}").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/helados/sale/{id}").hasRole("ADMIN_GLOBAL")
                        .requestMatchers(HttpMethod.DELETE, "/helados/sale/{id}").hasRole("ADMIN_GLOBAL")
                        .requestMatchers("/helados/sale/inventory/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/helados/cyInventory/**", "/helados/productInventory/**")
                        .hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        .requestMatchers(HttpMethod.PUT, "/helados/cyInventory/**", "/helados/productInventory/**")
                        .hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        .requestMatchers(HttpMethod.DELETE, "/helados/cyInventory/**", "/helados/productInventory/**")
                        .hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        .requestMatchers(HttpMethod.POST, "/helados/product", "/helados/cylinder")
                        .hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        .requestMatchers(HttpMethod.DELETE, "/helados/product/**", "/helados/cylinder/**")
                        .hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        .requestMatchers(HttpMethod.POST, "/helados/expenses/**")
                        .hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/helados/expenses/**")
                        .hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        .requestMatchers(HttpMethod.DELETE, "/helados/expenses/**")
                        .hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        //cylinder
                        .requestMatchers(HttpMethod.GET, "/helados/cylinder/**")
                        .hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        //cyInvent
                        .requestMatchers(HttpMethod.GET,    "/helados/cyInventory/**").authenticated()
                        //.requestMatchers(HttpMethod.POST,   "/helados/cyInventory/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        //.requestMatchers(HttpMethod.DELETE, "/helados/cyInventory/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        //.requestMatchers(HttpMethod.PUT,    "/helados/cyInventory/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        //Expense
                        .requestMatchers(HttpMethod.GET,    "/helados/expenses/**").authenticated()
                        //.requestMatchers(HttpMethod.POST,   "/helados/expenses/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        //.requestMatchers(HttpMethod.DELETE, "/helados/expenses/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        //product
                        .requestMatchers(HttpMethod.GET,    "/helados/product/**").authenticated()
                        //productInventory
                        .requestMatchers(HttpMethod.GET,    "/helados/productInventory/**").authenticated()
                        //.requestMatchers(HttpMethod.POST,   "/helados/productInventory/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        //.requestMatchers(HttpMethod.DELETE, "/helados/productInventory/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        //api/reports
                        .requestMatchers("/api/reports/**").hasRole("ADMIN_GLOBAL")
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService);

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 🔐 Provee el AuthenticationManager que usa Spring para validar credenciales.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 🧂 Codificador de contraseñas seguro (usa BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001", "http://localhost:5055", "http://rival:5055"));
        configuration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
