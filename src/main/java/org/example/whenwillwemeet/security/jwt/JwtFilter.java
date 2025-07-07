package org.example.whenwillwemeet.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.whenwillwemeet.common.exception.ApplicationException;
import org.example.whenwillwemeet.common.exception.ErrorCode;
import org.example.whenwillwemeet.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

  private final JwtUtils jwtUtils;

  @Value("${jwt.public-url}")
  private List<String> publicURLs;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String uri = request.getRequestURI();

    if (publicURLs.stream()
        .anyMatch(f -> f.equals(uri))) {
      filterChain.doFilter(request, response);
      return;
    }

    String tokenValue = jwtUtils.getTokenFromRequest(request);

    if (!(StringUtils.hasText(tokenValue) && jwtUtils.validateToken(tokenValue))) {
      throw new ApplicationException(ErrorCode.INVALID_TOKEN_EXCEPTION);
    }

    Claims claim = jwtUtils.getUserInfoFromToken(tokenValue);
    setUserIdToSecurityContextHolder(UUID.fromString(claim.get(JwtUtils.ID_KEY).toString()));

    filterChain.doFilter(request, response);
  }

  public void setUserIdToSecurityContextHolder(UUID userId) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    UserDetails userDetails = new UserDetailsImpl(userId);
    context.setAuthentication(
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    SecurityContextHolder.setContext(context);
  }
}
