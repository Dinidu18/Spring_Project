package lk.chapterhouse.web.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lk.chapterhouse.web.session.SessionKeys;

public final class Flash {
    private Flash() {
    }

    public static void success(HttpServletRequest request, String message) {
        request.getSession(true).setAttribute(SessionKeys.FLASH_SUCCESS, message);
    }

    public static void error(HttpServletRequest request, String message) {
        request.getSession(true).setAttribute(SessionKeys.FLASH_ERROR, message);
    }

    public static void promoteToRequest(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object ok = session.getAttribute(SessionKeys.FLASH_SUCCESS);
        if (ok instanceof String success) {
            request.setAttribute("flashSuccess", success);
            session.removeAttribute(SessionKeys.FLASH_SUCCESS);
        }
        Object err = session.getAttribute(SessionKeys.FLASH_ERROR);
        if (err instanceof String error) {
            request.setAttribute("flashError", error);
            session.removeAttribute(SessionKeys.FLASH_ERROR);
        }
    }
}
