package org.thluon.tdrive.filter;

import java.util.Collections;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.thluon.tdrive.security.MyPrincipal;
import reactor.core.publisher.Mono;

@Component
public class SecurityContextReconstructionFilter implements WebFilter {
  @Override
  @NonNull
  public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
    String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
    String userName = exchange.getRequest().getHeaders().getFirst("X-User-Name");
    String userRole = exchange.getRequest().getHeaders().getFirst("X-User-Role");
    System.out.println("reconstructing .. ");
    UUID id = null;
    if (userId != null && !userId.isEmpty()) {
      try {
        id = UUID.fromString(userId);
      } catch (IllegalArgumentException e) {
      }
    }
    if (userId != null && !userId.isEmpty() && id != null) {
      MyPrincipal principal = new MyPrincipal(id, userName, userRole);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              principal,
              null,
              Collections.singletonList(
                  new SimpleGrantedAuthority(
                      principal.role().startsWith("ROLE_")
                          ? principal.role()
                          : "ROLE_" + principal.role())));
      SecurityContext ctx = new SecurityContextImpl(authentication);
      return chain
          .filter(exchange)
          .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(ctx)));
    }
    return chain.filter(exchange);
  }
}
