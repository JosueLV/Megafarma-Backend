package pe.edu.upeu.megafarma.config;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import pe.edu.upeu.megafarma.security.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  @Bean
  SecurityFilterChain security(
      HttpSecurity http,
      JwtAuthenticationFilter filter,
      RespuestaErrorSeguridad errores,
      @org.springframework.beans.factory.annotation.Qualifier("cors")
          CorsConfigurationSource corsConfigurationSource)
      throws Exception {
    return http.csrf(c -> c.disable())
        .cors(c -> c.configurationSource(corsConfigurationSource))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/api/v1/auth/login",
                        "/actuator/health",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (req, res, ex) ->
                            errores.escribir(
                                req, res, 401, "NO_AUTORIZADO", "Inicia sesión para continuar"))
                    .accessDeniedHandler(
                        (req, res, ex) ->
                            errores.escribir(
                                req,
                                res,
                                403,
                                "SIN_PERMISO",
                                "No tienes permiso para esta operación")))
        .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  FilterRegistrationBean<JwtAuthenticationFilter> desactivarRegistroServlet(
      JwtAuthenticationFilter filter) {
    var registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  CorsConfigurationSource cors(@Value("${app.cors.origins}") String origins) {
    var c = new CorsConfiguration();
    c.setAllowedOrigins(
        Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key"));
    c.setExposedHeaders(List.of("Location", "Idempotency-Replayed"));
    c.setAllowCredentials(false);
    c.setMaxAge(3600L);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", c);
    return source;
  }
}
