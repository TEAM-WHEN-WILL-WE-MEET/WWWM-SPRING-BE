package org.example.whenwillwemeet.common.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.whenwillwemeet.common.aop.resolver.LoginUserIdResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final LoginUserIdResolver loginUserIdResolver;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(loginUserIdResolver);
  }
}
