package lk.chapterhouse.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.chapterhouse.model.Book;
import lk.chapterhouse.model.WishlistItem;
import lk.chapterhouse.web.dto.UserView;
import lk.chapterhouse.web.filter.SessionUserFilter;
import lk.chapterhouse.web.util.Flash;
import lk.chapterhouse.web.util.ServletUtil;
import lk.chapterhouse.web.util.WebContext;

@WebServlet(name = "wishlistServlet", urlPatterns = {"/wishlist", "/wishlist/add", "/wishlist/remove"})
public class WishlistServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserView user = requireUser(req, resp);
        if (user == null) {
            return;
        }
        List<WishlistItem> items = WebContext.wishlist(getServletContext()).listForUser(user.getId());
        List<Book> books = new ArrayList<>();
        for (WishlistItem item : items) {
            try {
                books.add(WebContext.books(getServletContext()).getById(item.getBookId()));
            } catch (IllegalArgumentException ignored) {
                // pruned on next list
            }
        }
        req.setAttribute("wishlistBooks", books);
        req.setAttribute("pageTitle", "My Wishlist");
        ServletUtil.render(req, resp, "wishlist/wishlist");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserView user = requireUser(req, resp);
        if (user == null) {
            return;
        }
        String bookId = ServletUtil.param(req, "bookId");
        String path = req.getServletPath();
        try {
            if ("/wishlist/add".equals(path)) {
                WebContext.wishlist(getServletContext()).add(user.getId(), bookId);
                Flash.success(req, "Added to your wishlist.");
                String returnTo = ServletUtil.param(req, "returnTo");
                if (returnTo.startsWith("/")) {
                    ServletUtil.redirect(req, resp, returnTo);
                } else {
                    ServletUtil.redirect(req, resp, "/books/detail?id=" + bookId);
                }
            } else if ("/wishlist/remove".equals(path)) {
                WebContext.wishlist(getServletContext()).remove(user.getId(), bookId);
                Flash.success(req, "Removed from wishlist.");
                ServletUtil.redirect(req, resp, "/wishlist");
            } else {
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (RuntimeException ex) {
            Flash.error(req, ex.getMessage());
            ServletUtil.redirect(req, resp, "/wishlist");
        }
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
