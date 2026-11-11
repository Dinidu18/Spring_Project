package lk.chapterhouse.web.filter;

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lk.chapterhouse.web.dto.UserView;
import lk.chapterhouse.web.session.AuthSession;
import lk.chapterhouse.web.util.Flash;
import lk.chapterhouse.web.util.WebContext;

@WebFilter(filterName = "sessionUserFilter", urlPatterns = "/*")
public class SessionUserFilter implements Filter {
    public static final String CURRENT_USER = "currentUser";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        Flash.promoteToRequest(req);
        Optional<UserView> current = AuthSession.resolveCurrentUser(req, WebContext.users(req.getServletContext()));
        current.ifPresent(user -> req.setAttribute(CURRENT_USER, user));
        chain.doFilter(request, response);
    }
}
