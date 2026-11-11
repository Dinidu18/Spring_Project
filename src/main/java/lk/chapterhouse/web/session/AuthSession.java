package lk.chapterhouse.web.session;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lk.chapterhouse.model.Role;
import lk.chapterhouse.model.User;
import lk.chapterhouse.repository.UserRepository;
import lk.chapterhouse.web.dto.UserView;

public final class AuthSession {
    private AuthSession() {
    }

    public static void bindAfterLogin(HttpServletRequest request, User user) {
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionKeys.USER_ID, user.getId());
        session.setAttribute(SessionKeys.USER_ROLE, user.getRole().name());
        session.setAttribute(SessionKeys.SESSION_VERSION, user.getSessionVersion());
    }

    public static void clear(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public static Optional<UserView> resolveCurrentUser(HttpServletRequest request, UserRepository users) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        Object userIdObj = session.getAttribute(SessionKeys.USER_ID);
        Object roleObj = session.getAttribute(SessionKeys.USER_ROLE);
        Object versionObj = session.getAttribute(SessionKeys.SESSION_VERSION);
        if (!(userIdObj instanceof String userId) || userId.isBlank()) {
            return Optional.empty();
        }
        if (!(versionObj instanceof Integer sessionVersion)) {
            session.invalidate();
            return Optional.empty();
        }
        Optional<User> userOpt = users.findById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return Optional.empty();
        }
        User user = userOpt.get();
        if (user.getSessionVersion() != sessionVersion) {
            session.invalidate();
            return Optional.empty();
        }
        Role sessionRole = roleObj instanceof String roleName ? Role.parse(roleName) : Role.MEMBER;
        if (user.getRole() != sessionRole) {
            session.setAttribute(SessionKeys.USER_ROLE, user.getRole().name());
        }
        return Optional.of(UserView.from(user));
    }

    public static Optional<String> userId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        Object value = session.getAttribute(SessionKeys.USER_ID);
        if (value instanceof String id && !id.isBlank()) {
            return Optional.of(id);
        }
        return Optional.empty();
    }

    public static void refreshSessionVersion(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(SessionKeys.SESSION_VERSION, user.getSessionVersion());
            session.setAttribute(SessionKeys.USER_ROLE, user.getRole().name());
        }
    }
}
