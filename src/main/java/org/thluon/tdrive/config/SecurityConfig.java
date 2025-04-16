package org.thluon.tdrive.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.thluon.tdrive.filter.SecurityContextReconstructionFilter;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
  @Value("${application.api.secure}")
  private Boolean secureApiDocEndpoints;

  @Value("#{'${SECURE_ACTUATORS:}'.split(',')}")
  private String[] SECURE_ACTUATORS;

  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(
            exchange ->
                exchange
                    .pathMatchers("actuator/health", "actuator/env")
                    .permitAll()
                    .pathMatchers("/swagger-ui/**", "/webjars/swagger-ui/**", "/v3/api-doc/**")
                    .access(
                        (mono, context) ->
                            Mono.just(
                                new AuthorizationDecision(
                                    !secureApiDocEndpoints
                                        || (mono.blockOptional()
                                            .map(
                                                auth ->
                                                    auth.isAuthenticated()
                                                        && auth.getAuthorities().stream()
                                                            .anyMatch(
                                                                a ->
                                                                    a.getAuthority()
                                                                        .equals("ROLE_Developer")))
                                            .orElse(false)))))
                    .pathMatchers(SECURE_ACTUATORS)
                    .hasRole("ROLE_SYSTEM_ADMIN")
                    .anyExchange()
                    .authenticated())
        .addFilterAt(
            new SecurityContextReconstructionFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
    return http.build();
  }
}
