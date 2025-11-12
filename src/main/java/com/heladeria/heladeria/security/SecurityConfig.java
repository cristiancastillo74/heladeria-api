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
                .csrf(csrf -> csrf.disable()) // Deshabilita CSRF para APIs REST
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        //login
                        .requestMatchers("/api/auth/**").permitAll() // Login y registro abiertos
                        //cylinder
                        .requestMatchers(HttpMethod.GET,    "/helados/cylinder/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/helados/cylinder/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        .requestMatchers(HttpMethod.DELETE, "/helados/cylinder/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        //cyInvent
                        .requestMatchers(HttpMethod.GET,    "/helados/cyInventory/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/helados/cyInventory/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        .requestMatchers(HttpMethod.DELETE, "/helados/cyInventory/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        .requestMatchers(HttpMethod.PUT,    "/helados/cyInventory/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        //Expense
                        .requestMatchers(HttpMethod.GET,    "/helados/expenses/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/helados/expenses/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        .requestMatchers(HttpMethod.DELETE, "/helados/expenses/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        //product
                        .requestMatchers(HttpMethod.GET,    "/helados/product/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/helados/product/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        .requestMatchers(HttpMethod.DELETE, "/helados/product/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        //productInventory
                        .requestMatchers(HttpMethod.GET,    "/helados/productInventory/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/helados/productInventory/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        .requestMatchers(HttpMethod.DELETE, "/helados/productInventory/**").hasAnyRole("ADMIN_GLOBAL", "ADMIN_BRANCH")
                        //api/reports
                        .requestMatchers("/api/reports/**").hasRole("ADMIN_GLOBAL")
                        .anyRequest().authenticated() // Todo lo demás protegido
                )
                .userDetailsService(userDetailsService);

        // 👇 Agregamos el filtro JWT antes del filtro de autenticación por username/password
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
}
