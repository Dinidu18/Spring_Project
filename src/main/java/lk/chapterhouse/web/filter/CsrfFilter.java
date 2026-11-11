package lk.chapterhouse.web.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.chapterhouse.web.util.CsrfTokenUtil;

@WebFilter(filterName = "csrfFilter", urlPatterns = "/*")
public class CsrfFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(true);
        String token = CsrfTokenUtil.ensureToken(session);
        req.setAttribute(CsrfTokenUtil.REQUEST_ATTR, token);

        if ("POST".equalsIgnoreCase(req.getMethod()) && !CsrfTokenUtil.validatePost(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }
        chain.doFilter(request, response);
    }
}
