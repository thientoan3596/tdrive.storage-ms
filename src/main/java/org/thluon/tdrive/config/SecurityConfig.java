package org.thluon.tdrive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.thluon.tdrive.filter.SecurityContextReconstructionFilter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/swagger-ui/**", "/webjars/swagger-ui/**", "/v3/api-doc/**").permitAll()
                        .anyExchange().authenticated())
                .addFilterAt(new SecurityContextReconstructionFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }
}
