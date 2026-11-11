package lk.chapterhouse.web.servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.chapterhouse.model.Category;
import lk.chapterhouse.service.BookService;
import lk.chapterhouse.service.CatalogPage;
import lk.chapterhouse.service.CatalogQuery;
import lk.chapterhouse.service.CategoryService;
import lk.chapterhouse.web.util.ServletUtil;
import lk.chapterhouse.web.util.WebContext;

@WebServlet(name = "categoryBrowseServlet", urlPatterns = {"/categories", "/categories/detail"})
public class CategoryBrowseServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/categories/detail".equals(req.getServletPath())) {
            showDetail(req, resp);
        } else {
            listCategories(req, resp);
        }
    }

    private void listCategories(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Category> categories = WebContext.categories(getServletContext()).listAll();
        req.setAttribute("categories", categories);
        req.setAttribute("pageTitle", "Categories");
        ServletUtil.render(req, resp, "categories/list");
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id = ServletUtil.param(req, "id");
        if (id.isBlank()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        CategoryService categories = WebContext.categories(getServletContext());
        BookService books = WebContext.books(getServletContext());
        try {
            Category category = categories.getById(id);
            CatalogPage page = books.search(new CatalogQuery(
                    null, id, null, null, CatalogQuery.SortField.TITLE, false,
                    ServletUtil.intParam(req, "page", 1), 12));
            req.setAttribute("category", category);
            req.setAttribute("catalog", page);
            req.setAttribute("pageTitle", category.getName());
            ServletUtil.render(req, resp, "categories/detail");
        } catch (IllegalArgumentException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
