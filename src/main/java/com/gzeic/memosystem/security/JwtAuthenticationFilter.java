package com.gzeic.memosystem.security;

import com.gzeic.memosystem.tenant.TenantContext;
import com.gzeic.memosystem.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器（Spring Security 版）
 *
 * 执行位置：
 * - 位于 Spring Security 过滤器链中（比 Controller 更早执行）
 *
 * 核心任务：
 * 1) 从 Authorization 读取 Bearer Token
 * 2) 校验 Token 是否有效
 * 3) 从 Token 中解析出 username/userId/tenantId
 * 4) 把 tenantId 写入 TenantContext（让 MyBatis-Plus 多租户插件能拿到）
 * 5) 构造 Authentication 写入 SecurityContext（让后续代码认为“已登录”）
 *
 * 教学提示：
 * - 我们在这里把 tenantId 作为“可信来源”写入 TenantContext（来自 JWT，而不是来自请求头）
 * - 这样可以避免用户伪造请求头来切换租户
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TenantUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtUtil.getUsernameFromToken(token);
        Long userId = jwtUtil.getUserIdFromToken(token);
        Long tenantId = jwtUtil.getTenantIdFromToken(token);

        if (tenantId != null) {
            TenantContext.setTenantId(tenantId);
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("tenantId", tenantId);

        filterChain.doFilter(request, response);
    }
}
