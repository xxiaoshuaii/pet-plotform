package com.pethub.interceptor;

import com.pethub.common.context.BaseContext;
import com.pethub.properties.JwtProperties;
import com.pethub.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtTokenInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader(jwtProperties.getTokenName());
        if (authorization == null || authorization.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"未登录或 token 为空\",\"data\":null}");
            return false;
        }

        String token = authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : authorization;

        try {
            Claims claims = jwtUtil.parseToken(token);
            Long adminId = claims.get("adminId", Long.class);
            Long userId = claims.get("userId", Long.class);
            Long currentId = adminId != null ? adminId : userId;
            String username = claims.get("username", String.class);

            request.setAttribute("adminId", adminId);
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);

            BaseContext.setCurrentId(currentId);
            BaseContext.setCurrentUsername(username);
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"token 无效或已过期\",\"data\":null}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.clear();
    }
}
