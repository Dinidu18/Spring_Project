package lk.chapterhouse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookstoreApplicationSmokeTest {
    private static final Path DATA_DIRECTORY = createDataDirectory();
    private static final Pattern BOOK_DETAIL_LINK =
            Pattern.compile("href=\"(/books/detail\\?id=[^\"]+)\"");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void homePageRendersFromTheFileBackedCatalog() {
        ResponseEntity<String> response = restTemplate.getForEntity("/", String.class);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Stories that stay with you"));
    }

    @Test
    void catalogAndBookDetailsRender() {
        ResponseEntity<String> catalog = restTemplate.getForEntity("/books", String.class);
        assertEquals(200, catalog.getStatusCode().value());

        Matcher link = BOOK_DETAIL_LINK.matcher(catalog.getBody());
        assertTrue(link.find(), "The catalog should link to a seeded book detail page.");

        ResponseEntity<String> detail = restTemplate.getForEntity(link.group(1), String.class);
        assertEquals(200, detail.getStatusCode().value());
        assertTrue(detail.getBody().contains("Rs. "));
    }

    @Test
    void staticAssetsAndCustomNotFoundPageAreAvailable() {
        ResponseEntity<String> stylesheet =
                restTemplate.getForEntity("/assets/css/site.css", String.class);
        assertEquals(200, stylesheet.getStatusCode().value());
        assertTrue(stylesheet.getBody().contains(".site-header"));

        ResponseEntity<String> notFound = restTemplate.getForEntity("/missing-page", String.class);
        assertEquals(404, notFound.getStatusCode().value());
        assertTrue(notFound.getBody().contains("Return home"));
    }

    @AfterAll
    static void removeTemporaryData() throws IOException {
        System.clearProperty("app.data.dir");
        System.clearProperty("chapterhouse.bootstrapAdminEmail");
        System.clearProperty("chapterhouse.bootstrapAdminPassword");
        try (var paths = Files.walk(DATA_DIRECTORY)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static Path createDataDirectory() {
        try {
            Path directory = Files.createTempDirectory("chapter-house-test-data-");
            System.setProperty("app.data.dir", directory.toString());
            System.setProperty("chapterhouse.bootstrapAdminEmail", "test-admin@example.test");
            System.setProperty("chapterhouse.bootstrapAdminPassword", "AdminTest123!");
            return directory;
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
}
