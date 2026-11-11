package lk.chapterhouse.web.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.chapterhouse.service.AvailableBooksPdfService;
import lk.chapterhouse.web.util.WebContext;

@WebServlet(name = "availableBooksPdfServlet", urlPatterns = {"/books/available.pdf"})
public class AvailableBooksPdfServlet extends HttpServlet {
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AvailableBooksPdfService pdfService = new AvailableBooksPdfService(
                WebContext.books(getServletContext()),
                WebContext.categories(getServletContext()));

        byte[] pdf = pdfService.buildPdf();
        String filename = "chapter-house-available-books-" + FILE_DATE.format(LocalDate.now()) + ".pdf";

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        resp.setContentLength(pdf.length);
        resp.getOutputStream().write(pdf);
        resp.getOutputStream().flush();
    }
}
