package com.huiyi.medical.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huiyi.medical.common.Result;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Resource private TokenStore tokenStore;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            writeError(response, "请先登录");
            return false;
        }
        Long userId = tokenStore.getUserId(token);
        if (userId == null) {
            writeError(response, "登录已过期，请重新登录");
            return false;
        }
        request.setAttribute("userId", userId);
        return true;
    }

    private void writeError(HttpServletResponse response, String message) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(mapper.writeValueAsString(Result.error(401, message)));
    }
}
