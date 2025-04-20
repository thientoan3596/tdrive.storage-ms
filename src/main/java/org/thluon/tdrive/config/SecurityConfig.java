package org.thluon.tdrive.config;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  @Value("${SPRINGDOC_SECURE}")
  private Boolean secureApiDocEndpoints;

  @Value(
      "#{ ('${SECURE_ACTUATORS:}').trim().isEmpty() ? new String[0] :"
          + " '${SECURE_ACTUATORS:}'.split(',') }")
  private String[] SECURE_ACTUATORS;

  @SneakyThrows
  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(
            exchange -> {
              var spec =
                  exchange
                      .pathMatchers("actuator/health", "actuator/env")
                      .permitAll()
                      .pathMatchers("/swagger-ui/**", "/webjars/swagger-ui/**", "/v3/api-doc/**")
                      .access(
                          (mono, context) ->
                              secureApiDocEndpoints
                                  ? mono.map(
                                          auth ->
                                              auth.isAuthenticated()
                                                  && auth.getAuthorities().stream()
                                                      .anyMatch(
                                                          a ->
                                                              a.getAuthority()
                                                                  .equals("ROLE_Developer")))
                                      .map(AuthorizationDecision::new)
                                      .defaultIfEmpty(new AuthorizationDecision(false))
                                  : Mono.just(new AuthorizationDecision(true)))
                      .anyExchange()
                      .permitAll();
              if (SECURE_ACTUATORS.length != 0) {
                spec.pathMatchers(SECURE_ACTUATORS).hasRole("ROLE_SYSTEM_ADMIN");
              }
            });
    return http.build();
  }
}
