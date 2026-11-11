package lk.chapterhouse.web.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

public final class ServletUtil {
    private ServletUtil() {
    }

    public static void render(HttpServletRequest request, HttpServletResponse response, String templateName)
            throws ServletException, IOException {
        Flash.promoteToRequest(request);
        WebApplicationContext context =
                WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
        SpringTemplateEngine templateEngine = context.getBean(SpringTemplateEngine.class);
        Context templateContext = new Context(request.getLocale());
        addAttributes(templateContext, request.getSession(false));
        addAttributes(templateContext, request);
        templateContext.setVariable("contextPath", request.getContextPath());
        templateContext.setVariable("requestPath", request.getServletPath());
        templateContext.setVariable("serverName", request.getServerName());
        templateContext.setVariable("param", requestParameters(request));
        templateContext.setVariable("bodyClass", "");
        templateContext.setVariable("assets", context.getBean(AssetView.class));

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        templateEngine.process(templateName, templateContext, response.getWriter());
    }

    private static void addAttributes(Context context, HttpSession session) {
        if (session == null) {
            return;
        }
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            context.setVariable(name, session.getAttribute(name));
        }
    }

    private static void addAttributes(Context context, HttpServletRequest request) {
        Enumeration<String> names = request.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            context.setVariable(name, request.getAttribute(name));
        }
    }

    private static Map<String, String> requestParameters(HttpServletRequest request) {
        Map<String, String> parameters = new LinkedHashMap<>();
        request.getParameterMap().forEach((name, values) -> {
            if (values.length > 0) {
                parameters.put(name, values[0]);
            }
        });
        return parameters;
    }

    public static void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        String ctx = request.getContextPath();
        response.sendRedirect(ctx + path);
    }

    public static String param(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    public static int intParam(HttpServletRequest request, String name, int defaultValue) {
        try {
            return Integer.parseInt(param(request, name));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public static boolean boolParam(HttpServletRequest request, String name) {
        return "on".equalsIgnoreCase(param(request, name))
                || "true".equalsIgnoreCase(param(request, name))
                || "1".equals(param(request, name));
    }

    public static Optional<BigDecimal> decimalParam(HttpServletRequest request, String name) {
        String raw = param(request, name);
        if (raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new BigDecimal(raw));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public static char[] passwordParam(HttpServletRequest request, String name) {
        return param(request, name).toCharArray();
    }
}
