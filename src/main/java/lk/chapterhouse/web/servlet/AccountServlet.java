package lk.chapterhouse.web.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.chapterhouse.model.User;
import lk.chapterhouse.web.dto.UserView;
import lk.chapterhouse.web.filter.SessionUserFilter;
import lk.chapterhouse.web.session.AuthSession;
import lk.chapterhouse.web.util.Flash;
import lk.chapterhouse.web.util.ServletUtil;
import lk.chapterhouse.web.util.WebContext;

@WebServlet(name = "accountServlet", urlPatterns = {
        "/account", "/account/edit", "/account/password", "/account/delete"
})
public class AccountServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserView user = requireUser(req, resp);
        if (user == null) {
            return;
        }
        String path = req.getServletPath();
        req.setAttribute("account", user);
        switch (path) {
            case "/account/edit" -> {
                req.setAttribute("pageTitle", "Edit Profile");
                ServletUtil.render(req, resp, "account/edit");
            }
            case "/account/password" -> {
                req.setAttribute("pageTitle", "Change Password");
                ServletUtil.render(req, resp, "account/password");
            }
            case "/account/delete" -> {
                req.setAttribute("pageTitle", "Delete Account");
                ServletUtil.render(req, resp, "account/delete");
            }
            default -> {
                req.setAttribute("pageTitle", "My Account");
                ServletUtil.render(req, resp, "account/account");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserView user = requireUser(req, resp);
        if (user == null) {
            return;
        }
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/account/edit" -> handleEdit(req, resp, user);
                case "/account/password" -> handlePassword(req, resp, user);
                case "/account/delete" -> handleDelete(req, resp, user);
                default -> throw new IllegalArgumentException("Unsupported action.");
            }
        } catch (RuntimeException ex) {
            Flash.error(req, ex.getMessage());
            ServletUtil.redirect(req, resp, path);
        }
    }

    private void handleEdit(HttpServletRequest req, HttpServletResponse resp, UserView user) throws IOException {
        User updated = WebContext.accounts(getServletContext()).updateProfile(
                user.getId(),
                ServletUtil.param(req, "displayName"),
                ServletUtil.param(req, "email"),
                ServletUtil.param(req, "phone"));
        req.setAttribute(SessionUserFilter.CURRENT_USER, UserView.from(updated));
        Flash.success(req, "Profile updated.");
        ServletUtil.redirect(req, resp, "/account");
    }

    private void handlePassword(HttpServletRequest req, HttpServletResponse resp, UserView user) throws IOException {
        char[] current = ServletUtil.passwordParam(req, "currentPassword");
        char[] next = ServletUtil.passwordParam(req, "newPassword");
        char[] confirm = ServletUtil.passwordParam(req, "confirmPassword");
        try {
            if (!String.valueOf(next).equals(String.valueOf(confirm))) {
                throw new IllegalArgumentException("New passwords do not match.");
            }
            User updated = WebContext.accounts(getServletContext()).changePassword(user.getId(), current, next);
            AuthSession.bindAfterLogin(req, updated);
            Flash.success(req, "Password changed. Please sign in again on other devices.");
            ServletUtil.redirect(req, resp, "/account");
        } finally {
            java.util.Arrays.fill(current, '\0');
            java.util.Arrays.fill(next, '\0');
            java.util.Arrays.fill(confirm, '\0');
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, UserView user) throws IOException {
        WebContext.accounts(getServletContext()).deleteAccount(user.getId());
        AuthSession.clear(req);
        Flash.success(req, "Your account has been deleted.");
        ServletUtil.redirect(req, resp, "/");
    }

    private UserView requireUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Object attr = req.getAttribute(SessionUserFilter.CURRENT_USER);
        if (attr instanceof UserView user) {
            return user;
        }
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        return null;
    }
}
