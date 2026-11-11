package lk.chapterhouse.web.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.chapterhouse.model.Book;
import lk.chapterhouse.model.Category;
import lk.chapterhouse.model.ContactMessage;
import lk.chapterhouse.model.EBook;
import lk.chapterhouse.model.PrintedBook;
import lk.chapterhouse.model.Review;
import lk.chapterhouse.model.User;
import lk.chapterhouse.repository.ContactRepository;
import lk.chapterhouse.repository.ReviewRepository;
import lk.chapterhouse.util.AssetManifest;
import lk.chapterhouse.web.AppAttributes;
import lk.chapterhouse.web.dto.UserView;
import lk.chapterhouse.web.filter.SessionUserFilter;
import lk.chapterhouse.web.util.Flash;
import lk.chapterhouse.web.util.ServletUtil;
import lk.chapterhouse.web.util.WebContext;

@WebServlet(name = "adminServlet", urlPatterns = {"/admin", "/admin/*"})
public class AdminServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (requireAdmin(req, resp) == null) {
            return;
        }
        String sub = subPath(req);
        switch (sub) {
            case "", "/" -> dashboard(req, resp);
            case "/books" -> booksList(req, resp);
            case "/books/new" -> bookForm(req, resp, null);
            case "/books/edit" -> bookForm(req, resp, ServletUtil.param(req, "id"));
            case "/books/delete" -> bookDeleteForm(req, resp);
            case "/categories" -> categoriesList(req, resp);
            case "/categories/new" -> categoryForm(req, resp, null);
            case "/categories/edit" -> categoryForm(req, resp, ServletUtil.param(req, "id"));
            case "/categories/delete" -> categoryDeleteForm(req, resp);
            case "/reviews" -> reviewsList(req, resp);
            case "/contact" -> contactList(req, resp);
            case "/contact/detail" -> contactDetail(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (requireAdmin(req, resp) == null) {
            return;
        }
        String sub = subPath(req);
        try {
            switch (sub) {
                case "/books/save" -> saveBook(req, resp);
                case "/books/delete" -> deleteBook(req, resp);
                case "/categories/save" -> saveCategory(req, resp);
                case "/categories/delete" -> deleteCategory(req, resp);
                case "/reviews/delete" -> deleteReview(req, resp);
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (RuntimeException ex) {
            Flash.error(req, ex.getMessage());
            if ("/books/save".equals(sub)) {
                String id = ServletUtil.param(req, "id");
                String path = id.isBlank()
                        ? "/admin/books/new"
                        : "/admin/books/edit?id=" + URLEncoder.encode(id, StandardCharsets.UTF_8);
                ServletUtil.redirect(req, resp, path);
            } else {
                ServletUtil.redirect(req, resp, "/admin");
            }
        }
    }

    private void dashboard(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("bookCount", WebContext.books(getServletContext()).listAll().size());
        req.setAttribute("categoryCount", WebContext.categories(getServletContext()).listAll().size());
        req.setAttribute("reviewCount", reviewsRepo().findAll().size());
        req.setAttribute("messageCount", WebContext.contact(getServletContext()).listAllForAdmin().size());
        req.setAttribute("pageTitle", "Admin Dashboard");
        ServletUtil.render(req, resp, "admin/dashboard");
    }

    private void booksList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("books", WebContext.books(getServletContext()).listAll());
        req.setAttribute("pageTitle", "Manage Books");
        ServletUtil.render(req, resp, "admin/books/list");
    }

    private void bookForm(HttpServletRequest req, HttpServletResponse resp, String id)
            throws ServletException, IOException {
        List<Category> categories = WebContext.categories(getServletContext()).listAll();
        req.setAttribute("categories", categories);
        req.setAttribute("coverKeys", AssetManifest.allEntries().keySet().stream()
                .filter(k -> k.startsWith("covers/"))
                .toList());
        if (id != null && !id.isBlank()) {
            try {
                req.setAttribute("book", WebContext.books(getServletContext()).getById(id));
            } catch (IllegalArgumentException ex) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            req.setAttribute("pageTitle", "Edit Book");
        } else {
            req.setAttribute("pageTitle", "New Book");
        }
        ServletUtil.render(req, resp, "admin/books/form");
    }

    private void bookDeleteForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id = ServletUtil.param(req, "id");
        try {
            req.setAttribute("book", WebContext.books(getServletContext()).getById(id));
        } catch (IllegalArgumentException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("pageTitle", "Delete Book");
        ServletUtil.render(req, resp, "admin/books/delete");
    }

    private void saveBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = ServletUtil.param(req, "id");
        String type = ServletUtil.param(req, "bookType");
        String title = ServletUtil.param(req, "title");
        String author = ServletUtil.param(req, "author");
        String description = ServletUtil.param(req, "description");
        String isbn = ServletUtil.param(req, "isbn");
        int year = ServletUtil.intParam(req, "publicationYear", 2020);
        String categoryId = ServletUtil.param(req, "categoryId");
        BigDecimal price = ServletUtil.decimalParam(req, "price").orElse(new BigDecimal("9.99"));
        String cover = ServletUtil.param(req, "coverImageKey");
        boolean featured = ServletUtil.boolParam(req, "featured");

        if (id.isBlank()) {
            if (PrintedBook.TYPE.equalsIgnoreCase(type)) {
                WebContext.books(getServletContext()).createPrinted(
                        title, author, description, isbn, year, categoryId, price, cover, featured,
                        ServletUtil.intParam(req, "stockQuantity", 0),
                        ServletUtil.intParam(req, "pageCount", 200),
                        ServletUtil.param(req, "binding"));
            } else {
                WebContext.books(getServletContext()).createEbook(
                        title, author, description, isbn, year, categoryId, price, cover, featured,
                        ServletUtil.param(req, "fileFormat"),
                        ServletUtil.decimalParam(req, "fileSizeMb").orElse(new BigDecimal("1.0")),
                        ServletUtil.boolParam(req, "digitalAvailable"));
            }
            Flash.success(req, "Book created.");
        } else {
            Book existing = WebContext.books(getServletContext()).getById(id);
            if (existing instanceof PrintedBook) {
                WebContext.books(getServletContext()).updatePrinted(
                        id, title, author, description, isbn, year, categoryId, price, cover, featured,
                        ServletUtil.intParam(req, "stockQuantity", 0),
                        ServletUtil.intParam(req, "pageCount", 200),
                        ServletUtil.param(req, "binding"));
            } else if (existing instanceof EBook) {
                WebContext.books(getServletContext()).updateEbook(
                        id, title, author, description, isbn, year, categoryId, price, cover, featured,
                        ServletUtil.param(req, "fileFormat"),
                        ServletUtil.decimalParam(req, "fileSizeMb").orElse(new BigDecimal("1.0")),
                        ServletUtil.boolParam(req, "digitalAvailable"));
            }
            Flash.success(req, "Book updated.");
        }
        ServletUtil.redirect(req, resp, "/admin/books");
    }

    private void deleteBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = ServletUtil.param(req, "id");
        WebContext.books(getServletContext()).delete(id);
        Flash.success(req, "Book deleted.");
        ServletUtil.redirect(req, resp, "/admin/books");
    }

    private void categoriesList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("categories", WebContext.categories(getServletContext()).listAll());
        req.setAttribute("pageTitle", "Manage Categories");
        ServletUtil.render(req, resp, "admin/categories/list");
    }

    private void categoryForm(HttpServletRequest req, HttpServletResponse resp, String id)
            throws ServletException, IOException {
        req.setAttribute("imageKeys", AssetManifest.allEntries().keySet().stream()
                .filter(k -> k.startsWith("categories/"))
                .toList());
        if (id != null && !id.isBlank()) {
            try {
                req.setAttribute("category", WebContext.categories(getServletContext()).getById(id));
            } catch (IllegalArgumentException ex) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            req.setAttribute("pageTitle", "Edit Category");
        } else {
            req.setAttribute("pageTitle", "New Category");
        }
        ServletUtil.render(req, resp, "admin/categories/form");
    }

    private void categoryDeleteForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id = ServletUtil.param(req, "id");
        try {
            req.setAttribute("category", WebContext.categories(getServletContext()).getById(id));
        } catch (IllegalArgumentException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("pageTitle", "Delete Category");
        ServletUtil.render(req, resp, "admin/categories/delete");
    }

    private void saveCategory(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = ServletUtil.param(req, "id");
        String name = ServletUtil.param(req, "name");
        String description = ServletUtil.param(req, "description");
        String imageKey = ServletUtil.param(req, "imageKey");
        if (id.isBlank()) {
            WebContext.categories(getServletContext()).create(name, description, imageKey);
            Flash.success(req, "Category created.");
        } else {
            WebContext.categories(getServletContext()).update(id, name, description, imageKey);
            Flash.success(req, "Category updated.");
        }
        ServletUtil.redirect(req, resp, "/admin/categories");
    }

    private void deleteCategory(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        WebContext.categories(getServletContext()).delete(ServletUtil.param(req, "id"));
        Flash.success(req, "Category deleted.");
        ServletUtil.redirect(req, resp, "/admin/categories");
    }

    private void reviewsList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Review> reviews = reviewsRepo().findAll();
        req.setAttribute("reviews", reviews);
        req.setAttribute("pageTitle", "Moderate Reviews");
        ServletUtil.render(req, resp, "admin/reviews/list");
    }

    private void deleteReview(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserView admin = (UserView) req.getAttribute(SessionUserFilter.CURRENT_USER);
        User user = WebContext.users(getServletContext()).findById(admin.getId()).orElseThrow();
        WebContext.reviews(getServletContext()).deleteByAdmin(ServletUtil.param(req, "id"), user);
        Flash.success(req, "Review removed.");
        ServletUtil.redirect(req, resp, "/admin/reviews");
    }

    private void contactList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("messages", WebContext.contact(getServletContext()).listAllForAdmin());
        req.setAttribute("pageTitle", "Contact Messages");
        ServletUtil.render(req, resp, "admin/contact/list");
    }

    private void contactDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id = ServletUtil.param(req, "id");
        Optional<ContactMessage> message = contactRepo().findById(id);
        if (message.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("message", message.get());
        req.setAttribute("pageTitle", "Message Detail");
        ServletUtil.render(req, resp, "admin/contact/detail");
    }

    private ReviewRepository reviewsRepo() {
        return (ReviewRepository) getServletContext().getAttribute(AppAttributes.REVIEW_REPOSITORY);
    }

    private ContactRepository contactRepo() {
        return (ContactRepository) getServletContext().getAttribute(AppAttributes.CONTACT_REPOSITORY);
    }

    private String subPath(HttpServletRequest req) {
        String path = req.getPathInfo();
        return path == null ? "" : path;
    }

    private UserView requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Object attr = req.getAttribute(SessionUserFilter.CURRENT_USER);
        if (attr instanceof UserView user && user.isAdmin()) {
            return user;
        }
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        return null;
    }
}
