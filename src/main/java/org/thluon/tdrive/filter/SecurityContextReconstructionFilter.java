package org.thluon.tdrive.filter;

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

import java.util.Collections;
import java.util.UUID;

@Component
public class SecurityContextReconstructionFilter implements WebFilter {
    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange,@NonNull WebFilterChain chain) {
        System.out.println("REACHED");
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String userName = exchange.getRequest().getHeaders().getFirst("X-User-Name");
        String userRole = exchange.getRequest().getHeaders().getFirst("X-User-Role");
        if(userId != null ) {
            MyPrincipal principal = new MyPrincipal(UUID.fromString(userId), userName, userRole);
            Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, Collections.singletonList(new SimpleGrantedAuthority(principal.role().startsWith("ROLE_") ? principal.role() : "ROLE_" + principal.role())));
            System.out.println(authentication);
            SecurityContext ctx = new SecurityContextImpl(authentication);
            return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(ctx)));
        }
        return chain.filter(exchange);
    }
}
