package com.gzeic.memosystem.config;

import com.gzeic.memosystem.security.JwtAuthenticationFilter;
import com.gzeic.memosystem.security.TenantUserDetailsService;
import com.gzeic.memosystem.tenant.TenantHeaderFilter;
import com.gzeic.memosystem.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 注册租户请求头过滤器（Filter 形态）
     *
     * 说明：
     * - 该 Filter 会从请求头读取 X-Tenant-Id 写入 TenantContext
     * - 并在请求结束时清理 ThreadLocal，避免线程池复用导致串租户
     */
    @Bean
    public TenantHeaderFilter tenantHeaderFilter() {
        return new TenantHeaderFilter();
    }

    /**
     * 注册 JWT 认证过滤器（Spring Security 版）
     *
     * 说明：
     * - 该过滤器会校验 Authorization: Bearer xxx
     * - 校验通过后把 Authentication 写入 SecurityContext，从而让 /api/** 需要登录的接口可访问
     * - 同时从 token 中解析 tenantId 并写入 TenantContext，保证多租户隔离使用“可信来源”
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil,
                                                           TenantUserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    /**
     * 配置Spring Security过滤器链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   TenantHeaderFilter tenantHeaderFilter,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.csrf(csrf -> csrf.disable());

        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"无权限访问\",\"data\":null}");
                })
        );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(tenantHeaderFilter, JwtAuthenticationFilter.class);

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                        "/api/auth/**",
                        "/",
                        "/login",
                        "/register",
                        "/memos",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/css/**",
                        "/js/**",
                        "/favicon.ico"
                ).permitAll()
                .anyRequest().authenticated()
        );
        return http.build();
    }
}
