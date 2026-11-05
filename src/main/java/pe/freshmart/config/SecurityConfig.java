package pe.freshmart.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pe.freshmart.auth.JwtFilter;

@Configuration @EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain chain(HttpSecurity http, JwtFilter jwtFilter, @Value("${app.frontend-url}") String front) throws Exception {
        http.csrf(c -> c.disable()) // API stateless con JWT en cabecera, sin cookies
            .cors(c -> c.configurationSource(cors(front)))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/v1/auth/registro", "/api/v1/auth/login", "/api/v1/auth/recuperar", "/api/v1/auth/restablecer", "/api/v1/faq/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/catalogo/**", "/api/v1/tiendas/**").permitAll()
                .requestMatchers("/api/v1/personal/**").hasRole("PERSONAL")
                .requestMatchers("/api/v1/carrito/**", "/api/v1/pedidos/**").hasRole("CLIENTE")
                .anyRequest().authenticated())
            .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> {
                res.setStatus(401); res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"mensaje\":\"Tu sesión no es válida o venció. Inicia sesión para continuar.\"}");
            }))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    private CorsConfigurationSource cors(String front) {
        var c = new CorsConfiguration();
        c.setAllowedOrigins(List.of(front));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        var s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/api/**", c);
        return s;
    }
}
