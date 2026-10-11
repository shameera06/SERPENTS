package com.shareptiles.app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // Allow login endpoints, static assets, and H2 console
        if (uri.startsWith("/assets/") || uri.startsWith("/uploads/") || uri.startsWith("/samples/")
                || uri.equals("/admin/login") || uri.equals("/admin/login/submit")
                || uri.startsWith("/h2-console")) {
            return true;
        }

        // Check admin session
        if (uri.startsWith("/admin")) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("currentUser") == null) {
                response.sendRedirect("/admin/login");
                return false;
            }
        }

        return true;
    }
}
