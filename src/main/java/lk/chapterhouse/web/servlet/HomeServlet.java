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
import lk.chapterhouse.service.CatalogQuery;
import lk.chapterhouse.service.CategoryService;
import lk.chapterhouse.web.util.ServletUtil;
import lk.chapterhouse.web.util.WebContext;

@WebServlet(name = "homeServlet", urlPatterns = {"", "/home"})
public class HomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        BookService books = WebContext.books(getServletContext());
        CategoryService categories = WebContext.categories(getServletContext());

        List<Category> categoryList = categories.listAll();
        var featured = books.search(new CatalogQuery(null, null, null, true, CatalogQuery.SortField.TITLE, false, 1, 6));
        var newest = books.search(new CatalogQuery(null, null, null, null, CatalogQuery.SortField.CREATED, true, 1, 6));

        req.setAttribute("categories", categoryList);
        req.setAttribute("featuredBooks", featured.items());
        req.setAttribute("newArrivals", newest.items());
        req.setAttribute("pageTitle", "Chapter House — Home");
        ServletUtil.render(req, resp, "home");
    }
}
