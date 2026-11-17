package com.skillswap.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  public JwtAuthenticationFilter(JwtService jwtService) { this.jwtService = jwtService; }
  @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    var header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      try {
        var userId = jwtService.subject(header.substring(7));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));
      } catch (RuntimeException ignored) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); return;
      }
    }
    chain.doFilter(request, response);
  }
}
