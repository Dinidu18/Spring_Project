package lk.chapterhouse.web.util;

import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lk.chapterhouse.web.session.SessionKeys;

public final class CsrfTokenUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    public static final String REQUEST_ATTR = "csrfToken";
    public static final String PARAM_NAME = "_csrf";

    private CsrfTokenUtil() {
    }

    public static String ensureToken(HttpSession session) {
        Object existing = session.getAttribute(SessionKeys.CSRF_TOKEN);
        if (existing instanceof String token && !token.isBlank()) {
            return token;
        }
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(SessionKeys.CSRF_TOKEN, token);
        return token;
    }

    public static boolean validatePost(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object expectedObj = session.getAttribute(SessionKeys.CSRF_TOKEN);
        if (!(expectedObj instanceof String expected) || expected.isBlank()) {
            return false;
        }
        String submitted = request.getParameter(PARAM_NAME);
        return expected.equals(submitted);
    }
}
