package org.example.whenwillwemeet.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class IndexController {
    @GetMapping("/")
    public Object index(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        // ELB Health Checker 요청 필터링
        // ELB Health Checker 요청이면 200 OK 응답만 반환
        if (userAgent != null && userAgent.startsWith("ELB-HealthChecker"))
            return ResponseEntity.ok().build();

        // 인덱스 페이지는 접근할 일이 없기 때문에
        // 인덱스 페이지 접근시 요청한 Client의 IP와 Agent를 로깅
        log.info("[IndexController]-[index] Client IP: {}", clientIp);
        log.info("[IndexController]-[index] User-Agent: {}", userAgent);

        return "index";
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
