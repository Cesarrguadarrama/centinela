package mx.centinela.bootstrap.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS for local frontend development (`ng serve` on :4200 talking to :8080 directly). In the
 * composed deployment nginx proxies /api on the same origin, so this never kicks in there.
 */
@Configuration
class WebConfig implements WebMvcConfigurer {

  private final String[] allowedOrigins;

  WebConfig(@Value("${centinela.cors.allowed-origins:http://localhost:4200}") String origins) {
    this.allowedOrigins = origins.split(",");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins(allowedOrigins)
        .allowedMethods("GET", "POST", "PUT", "DELETE");
  }
}
