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

/**
 * JWT 登录拦截器。
 * 作用：
 * 1. 从请求头里取出 token
 * 2. 校验 token 是否合法
 * 3. 合法则放行，并把当前登录人写入 BaseContext
 * 4. 请求结束后清理 BaseContext
 */
@Component
@RequiredArgsConstructor
public class JwtTokenInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 浏览器预检请求直接放行。
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader(jwtProperties.getTokenName());
        if (authorization == null || authorization.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"未登录或token为空\",\"data\":null}");
            return false;
        }

        // 前端当前传的是 Bearer token，这里去掉前缀后再解析。
        String token = authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : authorization;

        try {
            Claims claims = jwtUtil.parseToken(token);
            Long adminId = claims.get("adminId", Long.class);
            String username = claims.get("username", String.class);

            // request 中留一份，方便控制器直接取。
            request.setAttribute("adminId", adminId);
            request.setAttribute("username", username);

            // BaseContext 中也留一份，方便业务层直接获取当前登录人。
            BaseContext.setCurrentId(adminId);
            BaseContext.setCurrentUsername(username);
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"token无效或已过期\",\"data\":null}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.clear();
    }
}
