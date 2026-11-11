package lk.chapterhouse.web.filter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.chapterhouse.model.Role;
import lk.chapterhouse.web.dto.UserView;
import lk.chapterhouse.web.session.AuthSession;
import lk.chapterhouse.web.util.ReturnPathUtil;
import lk.chapterhouse.web.util.WebContext;

@WebFilter(filterName = "authFilter", urlPatterns = {
        "/account", "/account/*",
        "/wishlist", "/wishlist/*",
        "/my", "/my/*",
        "/reviews/new", "/reviews/edit", "/reviews/delete",
        "/admin", "/admin/*"
})
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        Optional<UserView> user = AuthSession.resolveCurrentUser(req, WebContext.users(req.getServletContext()));
        if (user.isEmpty()) {
            String returnPath = safeReturnPath(req);
            String login = req.getContextPath() + "/login?return=" + URLEncoder.encode(returnPath, StandardCharsets.UTF_8);
            resp.sendRedirect(login);
            return;
        }

        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (path.startsWith("/admin") && user.get().getRole() != Role.ADMIN) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        req.setAttribute(SessionUserFilter.CURRENT_USER, user.get());
        chain.doFilter(request, response);
    }

    static String safeReturnPath(HttpServletRequest req) {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        String query = req.getQueryString();
        String full = query == null || query.isBlank() ? path : path + "?" + query;
        return ReturnPathUtil.sanitize(full);
    }
}
