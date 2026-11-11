package lk.chapterhouse.web.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public final class ReturnPathUtil {
    private static final Set<String> WHITELIST_PREFIXES = Set.of(
            "/account", "/wishlist", "/my", "/reviews", "/admin", "/books", "/categories");

    private ReturnPathUtil() {
    }

    public static String sanitize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "/";
        }
        String decoded = URLDecoder.decode(raw.trim(), StandardCharsets.UTF_8);
        if (!decoded.startsWith("/") || decoded.startsWith("//")) {
            return "/";
        }
        int q = decoded.indexOf('?');
        String path = q < 0 ? decoded : decoded.substring(0, q);
        String query = q < 0 ? null : decoded.substring(q + 1);
        String full = query == null ? path : path + "?" + query;
        for (String prefix : WHITELIST_PREFIXES) {
            if (full.equals(prefix) || full.startsWith(prefix + "/") || full.startsWith(prefix + "?")) {
                return full;
            }
        }
        return "/";
    }
}
