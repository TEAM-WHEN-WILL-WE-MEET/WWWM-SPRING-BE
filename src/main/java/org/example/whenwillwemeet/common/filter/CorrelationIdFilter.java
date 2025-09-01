package org.example.whenwillwemeet.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(1)
public class CorrelationIdFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String corrId = Optional.ofNullable(req.getHeader("X-Correlation-Id"))
        .orElse(UUID.randomUUID().toString());
    MDC.put("corrId", corrId.substring(0, 2)+"-"+corrId.substring(10, 12));
    try {
      chain.doFilter(req, res);
    } finally {
      MDC.clear();
    }
  }
}
