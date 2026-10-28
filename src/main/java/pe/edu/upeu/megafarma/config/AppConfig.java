package pe.edu.upeu.megafarma.config;

import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class AppConfig {
  @Bean
  Clock clock(@Value("${app.zona-horaria}") String zona) {
    return Clock.system(ZoneId.of(zona));
  }
}
