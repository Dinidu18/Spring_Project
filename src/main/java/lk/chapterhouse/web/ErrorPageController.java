package lk.chapterhouse.web;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.chapterhouse.web.util.ServletUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {
    @GetMapping("/error/403")
    public void forbidden(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletUtil.render(request, response, "error-403");
    }

    @GetMapping("/error/404")
    public void notFound(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletUtil.render(request, response, "error-404");
    }

    @GetMapping("/error/500")
    public void internalServerError(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletUtil.render(request, response, "error-500");
    }
}
