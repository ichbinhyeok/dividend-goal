package org.example.dividendgoal.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dividendgoal.seo.CanonicalUrls;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CanonicalRedirectFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String host = firstHeaderValue(request, "X-Forwarded-Host");
        if (host == null || host.isBlank()) {
            host = request.getServerName();
        }

        return CanonicalUrls.isLocalHost(host) || request.getRequestURI().startsWith("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String host = firstHeaderValue(request, "X-Forwarded-Host");
        if (host == null || host.isBlank()) {
            host = request.getServerName();
        }

        String forwardedProto = firstHeaderValue(request, "X-Forwarded-Proto");
        String scheme = (forwardedProto == null || forwardedProto.isBlank())
                ? (request.isSecure() ? "https" : request.getScheme())
                : forwardedProto;

        if (CanonicalUrls.isCanonicalHost(host) && "https".equalsIgnoreCase(scheme)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(308);
        response.setHeader("Location", CanonicalUrls.fromRequest(request));
    }

    private String firstHeaderValue(HttpServletRequest request, String name) {
        String header = request.getHeader(name);
        if (header == null || header.isBlank()) {
            return null;
        }
        return header.split(",")[0].trim();
    }
}
