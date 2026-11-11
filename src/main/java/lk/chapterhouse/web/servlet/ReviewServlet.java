package lk.chapterhouse.web.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.chapterhouse.model.Book;
import lk.chapterhouse.model.Review;
import lk.chapterhouse.model.User;
import lk.chapterhouse.repository.ReviewRepository;
import lk.chapterhouse.repository.UserRepository;
import lk.chapterhouse.web.AppAttributes;
import lk.chapterhouse.web.dto.UserView;
import lk.chapterhouse.web.filter.SessionUserFilter;
import lk.chapterhouse.web.util.Flash;
import lk.chapterhouse.web.util.ServletUtil;
import lk.chapterhouse.web.util.WebContext;

@WebServlet(name = "reviewServlet", urlPatterns = {
        "/reviews/new", "/reviews/detail", "/reviews/edit", "/reviews/delete", "/my/reviews"
})
public class ReviewServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        switch (path) {
            case "/reviews/new" -> showNew(req, resp);
            case "/reviews/edit" -> showEdit(req, resp);
            case "/reviews/delete" -> showDelete(req, resp);
            case "/my/reviews" -> showMine(req, resp);
            default -> showDetail(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();
        switch (path) {
            case "/reviews/new" -> create(req, resp);
            case "/reviews/edit" -> update(req, resp);
            case "/reviews/delete" -> delete(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void showNew(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserView user = requireUser(req, resp);
        if (user == null) {
            return;
        }
        String bookId = ServletUtil.param(req, "bookId");
        Book book = findBookOrNotFound(bookId, resp);
        if (book == null) {
            return;
        }
        req.setAttribute("book", book);
        req.setAttribute("pageTitle", "Write a Review");
        ServletUtil.render(req, resp, "reviews/new");
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Review review = loadReview(req, resp);
        if (review == null) {
            return;
        }
        Book book = findBookOrNotFound(review.getBookId(), resp);
        if (book == null) {
            return;
        }
        req.setAttribute("review", review);
        req.setAttribute("book", book);
        req.setAttribute("authorName", resolveDisplayName(review.getAuthorId()));
        req.setAttribute("pageTitle", review.getTitle());
        ServletUtil.render(req, resp, "reviews/detail");
    }

    private void showEdit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserView user = requireUser(req, resp);
        if (user == null) {
            return;
        }
        Review review = loadOwnedReview(req, resp, user);
        if (review == null) {
            return;
        }
        req.setAttribute("review", review);
        req.setAttribute("pageTitle", "Edit Review");
        ServletUtil.render(req, resp, "reviews/edit");
    }

    private void showDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserView user = requireUser(req, resp);
        if (user == null) {
            return;
        }
        Review review = loadOwnedReview(req, resp, user);
        if (review == null) {
            return;
        }
        req.setAttribute("review", review);
        req.setAttribute("pageTitle", "Delete Review");
        ServletUtil.render(req, resp, "reviews/delete");
    }

    private void showMine(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserView user = requireUser(req, resp);
        if (user == null) {
            return;
        }
        List<Review> reviews = WebContext.reviews(getServletContext()).listForAuthor(user.getId());
        Map<String, Book> books = new HashMap<>();
        for (Review review : reviews) {
            books.computeIfAbsent(review.getBookId(), id -> {
                try {
                    return WebContext.books(getServletContext()).getById(id);
                } catch (IllegalArgumentException ex) {
                    return null;
                }
            });
        }
        req.setAttribute("reviews", reviews);
        req.setAttribute("booksById", books);
        req.setAttribute("pageTitle", "My Reviews");
        ServletUtil.render(req, resp, "reviews/my");
    }

    private void create(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserView user = requireUser(req, resp);
        if (user == null) {
            return;
        }
        String bookId = ServletUtil.param(req, "bookId");
        try {
            Review review = WebContext.reviews(getServletContext()).create(
                    bookId,
                    user.getId(),
                    ServletUtil.intParam(req, "rating", 5),
                    ServletUtil.param(req, "title"),
                    ServletUtil.param(req, "body"));
            Flash.success(req, "Review published.");
            ServletUtil.redirect(req, resp, "/reviews/detail?id=" + review.getId());
        } catch (RuntimeException ex) {
            Flash.error(req, ex.getMessage());
            ServletUtil.redirect(req, resp, "/reviews/new?bookId=" + bookId);
        }
    }

    private void update(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserView user = requireUser(req, resp);
        if (user == null) {
            return;
        }
        String id = ServletUtil.param(req, "id");
        try {
            Review review = WebContext.reviews(getServletContext()).updateByAuthor(
                    id,
                    user.getId(),
                    ServletUtil.intParam(req, "rating", 5),
                    ServletUtil.param(req, "title"),
                    ServletUtil.param(req, "body"));
            Flash.success(req, "Review updated.");
            ServletUtil.redirect(req, resp, "/reviews/detail?id=" + review.getId());
        } catch (RuntimeException ex) {
            Flash.error(req, ex.getMessage());
            ServletUtil.redirect(req, resp, "/reviews/edit?id=" + id);
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserView user = requireUser(req, resp);
        if (user == null) {
            return;
        }
        String id = ServletUtil.param(req, "id");
        try {
            WebContext.reviews(getServletContext()).deleteByAuthor(id, user.getId());
            Flash.success(req, "Review deleted.");
            ServletUtil.redirect(req, resp, "/my/reviews");
        } catch (RuntimeException ex) {
            Flash.error(req, ex.getMessage());
            ServletUtil.redirect(req, resp, "/reviews/delete?id=" + id);
        }
    }

    private Review loadReview(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return findReviewById(ServletUtil.param(req, "id"), resp);
    }

    private Review findReviewById(String id, HttpServletResponse resp) throws IOException {
        if (id == null || id.isBlank()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        ReviewRepository reviewRepo = (ReviewRepository) getServletContext().getAttribute(AppAttributes.REVIEW_REPOSITORY);
        Optional<Review> review = reviewRepo.findById(id);
        if (review.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        return review.get();
    }

    private Review loadOwnedReview(HttpServletRequest req, HttpServletResponse resp, UserView user)
            throws IOException {
        Review review = findReviewById(ServletUtil.param(req, "id"), resp);
        if (review == null) {
            return null;
        }
        if (!review.getAuthorId().equals(user.getId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return review;
    }

    private Book findBookOrNotFound(String bookId, HttpServletResponse resp) throws IOException {
        if (bookId == null || bookId.isBlank()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        try {
            return WebContext.books(getServletContext()).getById(bookId);
        } catch (IllegalArgumentException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    private String resolveDisplayName(String userId) {
        UserRepository users = WebContext.users(getServletContext());
        return users.findById(userId).map(User::getDisplayName).orElse("Reader");
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
