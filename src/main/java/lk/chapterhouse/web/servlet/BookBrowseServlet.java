package lk.chapterhouse.web.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.chapterhouse.model.Book;
import lk.chapterhouse.model.Category;
import lk.chapterhouse.model.Review;
import lk.chapterhouse.model.User;
import lk.chapterhouse.repository.UserRepository;
import lk.chapterhouse.service.BookService;
import lk.chapterhouse.service.CatalogPage;
import lk.chapterhouse.service.CatalogQuery;
import lk.chapterhouse.service.CategoryService;
import lk.chapterhouse.service.ReviewService;
import lk.chapterhouse.web.util.ServletUtil;
import lk.chapterhouse.web.util.WebContext;

@WebServlet(name = "bookBrowseServlet", urlPatterns = {"/books", "/books/detail"})
public class BookBrowseServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String servletPath = req.getServletPath();
        if ("/books/detail".equals(servletPath)) {
            showDetail(req, resp);
        } else {
            listBooks(req, resp);
        }
    }

    private void listBooks(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        BookService books = WebContext.books(getServletContext());
        CategoryService categories = WebContext.categories(getServletContext());

        CatalogQuery query = new CatalogQuery(
                emptyToNull(ServletUtil.param(req, "q")),
                emptyToNull(ServletUtil.param(req, "categoryId")),
                emptyToNull(ServletUtil.param(req, "type")),
                boolOrNull(ServletUtil.param(req, "featured")),
                CatalogQuery.SortField.parse(ServletUtil.param(req, "sort")),
                "desc".equalsIgnoreCase(ServletUtil.param(req, "dir")),
                ServletUtil.intParam(req, "page", 1),
                ServletUtil.intParam(req, "size", 12));

        CatalogPage page = books.search(query);
        List<Category> categoryList = categories.listAll();

        req.setAttribute("catalog", page);
        req.setAttribute("categories", categoryList);
        req.setAttribute("query", query);
        req.setAttribute("pageTitle", "Browse Books");
        ServletUtil.render(req, resp, "books/list");
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id = ServletUtil.param(req, "id");
        if (id.isBlank()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        BookService books = WebContext.books(getServletContext());
        CategoryService categories = WebContext.categories(getServletContext());
        ReviewService reviews = WebContext.reviews(getServletContext());

        try {
            Book book = books.getById(id);
            Category category = categories.getById(book.getCategoryId());
            List<Review> bookReviews = reviews.listForBook(id);
            OptionalDouble avg = reviews.averageRatingForBook(id);
            UserRepository users = WebContext.users(getServletContext());
            Map<String, String> authorNames = new HashMap<>();
            for (Review review : bookReviews) {
                authorNames.putIfAbsent(review.getAuthorId(),
                        users.findById(review.getAuthorId()).map(User::getDisplayName).orElse("Reader"));
            }

            req.setAttribute("book", book);
            req.setAttribute("authorNames", authorNames);
            req.setAttribute("category", category);
            req.setAttribute("reviews", bookReviews);
            req.setAttribute("averageRating", avg.isPresent() ? avg.getAsDouble() : null);
            req.setAttribute("pageTitle", book.getTitle());
            ServletUtil.render(req, resp, "books/detail");
        } catch (IllegalArgumentException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static Boolean boolOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return "1".equals(value) || "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value);
    }
}
