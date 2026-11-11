package lk.chapterhouse.web.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.chapterhouse.model.User;
import lk.chapterhouse.web.session.AuthSession;
import lk.chapterhouse.web.util.Flash;
import lk.chapterhouse.web.util.ReturnPathUtil;
import lk.chapterhouse.web.util.ServletUtil;
import lk.chapterhouse.web.util.WebContext;

@WebServlet(name = "authServlet", urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/logout".equals(path)) {
            AuthSession.clear(req);
            Flash.success(req, "You have been signed out.");
            ServletUtil.redirect(req, resp, "/");
            return;
        }
        if (AuthSession.userId(req).isPresent()) {
            ServletUtil.redirect(req, resp, "/account");
            return;
        }
        if ("/register".equals(path)) {
            req.setAttribute("pageTitle", "Create Account");
            ServletUtil.render(req, resp, "register");
        } else {
            req.setAttribute("returnPath", ReturnPathUtil.sanitize(ServletUtil.param(req, "return")));
            req.setAttribute("pageTitle", "Sign In");
            ServletUtil.render(req, resp, "login");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();
        if ("/logout".equals(path)) {
            AuthSession.clear(req);
            ServletUtil.redirect(req, resp, "/");
            return;
        }
        if ("/register".equals(path)) {
            handleRegister(req, resp);
        } else {
            handleLogin(req, resp);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = ServletUtil.param(req, "email");
        char[] password = ServletUtil.passwordParam(req, "password");
        String returnPath = ReturnPathUtil.sanitize(ServletUtil.param(req, "return"));
        try {
            User user = WebContext.auth(getServletContext()).login(email, password);
            AuthSession.bindAfterLogin(req, user);
            Flash.success(req, "Welcome back, " + user.getDisplayName() + ".");
            ServletUtil.redirect(req, resp, returnPath);
        } catch (RuntimeException ex) {
            Flash.error(req, ex.getMessage());
            ServletUtil.redirect(req, resp, "/login?return=" + URLEncoder.encode(returnPath, StandardCharsets.UTF_8));
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String displayName = ServletUtil.param(req, "displayName");
        String email = ServletUtil.param(req, "email");
        String phone = ServletUtil.param(req, "phone");
        char[] password = ServletUtil.passwordParam(req, "password");
        char[] confirm = ServletUtil.passwordParam(req, "confirmPassword");
        try {
            if (!String.valueOf(password).equals(String.valueOf(confirm))) {
                throw new IllegalArgumentException("Passwords do not match.");
            }
            User user = WebContext.accounts(getServletContext())
                    .registerMember(displayName, email, phone, password);
            AuthSession.bindAfterLogin(req, user);
            Flash.success(req, "Account created. Welcome to Chapter House!");
            ServletUtil.redirect(req, resp, "/account");
        } catch (RuntimeException ex) {
            Flash.error(req, ex.getMessage());
            ServletUtil.redirect(req, resp, "/register");
        } finally {
            java.util.Arrays.fill(password, '\0');
            java.util.Arrays.fill(confirm, '\0');
        }
    }
}
