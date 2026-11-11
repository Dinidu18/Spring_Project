package lk.chapterhouse.web.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.chapterhouse.web.dto.UserView;
import lk.chapterhouse.web.filter.SessionUserFilter;
import lk.chapterhouse.web.session.AuthSession;
import lk.chapterhouse.web.util.Flash;
import lk.chapterhouse.web.util.ServletUtil;
import lk.chapterhouse.web.util.WebContext;

@WebServlet(name = "contactServlet", urlPatterns = {"/contact"})
public class ContactServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserView user = (UserView) req.getAttribute(SessionUserFilter.CURRENT_USER);
        if (user != null) {
            req.setAttribute("formName", user.getDisplayName());
            req.setAttribute("formEmail", user.getEmail());
        }
        req.setAttribute("pageTitle", "Contact Us");
        ServletUtil.render(req, resp, "contact");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = ServletUtil.param(req, "name");
        String email = ServletUtil.param(req, "email");
        String subject = ServletUtil.param(req, "subject");
        String message = ServletUtil.param(req, "message");
        String userId = AuthSession.userId(req).orElse(null);
        try {
            WebContext.contact(getServletContext()).submit(userId, name, email, subject, message);
            Flash.success(req, "Thank you — your message has been sent.");
            ServletUtil.redirect(req, resp, "/contact");
        } catch (RuntimeException ex) {
            Flash.error(req, ex.getMessage());
            ServletUtil.redirect(req, resp, "/contact");
        }
    }
}
