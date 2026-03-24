package org.example.dividendgoal.seo;

import jakarta.servlet.http.HttpServletRequest;
import org.example.dividendgoal.AppConstants;

import java.net.URI;

public final class CanonicalUrls {

    private static final URI BASE_URI = URI.create(AppConstants.BASE_URL);
    private static final String CANONICAL_HOST = BASE_URI.getHost();

    private CanonicalUrls() {
    }

    public static String fromRequest(HttpServletRequest request) {
        return withQuery(request.getRequestURI(), request.getQueryString());
    }

    public static String absolutePath(String path) {
        String normalizedPath = path == null || path.isBlank() ? "/" : path;
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }
        return AppConstants.BASE_URL + normalizedPath;
    }

    public static String withQuery(String path, String query) {
        if (query == null || query.isBlank()) {
            return absolutePath(path);
        }
        return absolutePath(path) + "?" + query;
    }

    public static boolean isCanonicalHost(String host) {
        return CANONICAL_HOST.equalsIgnoreCase(normalizeHost(host));
    }

    public static boolean isLocalHost(String host) {
        String normalizedHost = normalizeHost(host);
        return normalizedHost.isBlank()
                || "localhost".equalsIgnoreCase(normalizedHost)
                || "127.0.0.1".equals(normalizedHost)
                || "0.0.0.0".equals(normalizedHost);
    }

    private static String normalizeHost(String host) {
        if (host == null) {
            return "";
        }

        String firstHost = host.split(",")[0].trim();
        int portSeparator = firstHost.indexOf(':');
        if (portSeparator >= 0) {
            return firstHost.substring(0, portSeparator);
        }
        return firstHost;
    }
}
