package lk.chapterhouse.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lk.chapterhouse.repository.BookRepository;
import lk.chapterhouse.repository.CategoryRepository;
import lk.chapterhouse.repository.ContactRepository;
import lk.chapterhouse.repository.FileBookRepository;
import lk.chapterhouse.repository.FileCategoryRepository;
import lk.chapterhouse.repository.FileContactRepository;
import lk.chapterhouse.repository.FileReviewRepository;
import lk.chapterhouse.repository.FileUserRepository;
import lk.chapterhouse.repository.FileWishlistRepository;
import lk.chapterhouse.repository.ReviewRepository;
import lk.chapterhouse.repository.UserRepository;
import lk.chapterhouse.repository.WishlistRepository;
import lk.chapterhouse.seed.SeedData;
import lk.chapterhouse.service.AccountService;
import lk.chapterhouse.service.AuthService;
import lk.chapterhouse.service.BookService;
import lk.chapterhouse.service.CategoryService;
import lk.chapterhouse.service.ContactService;
import lk.chapterhouse.service.ReviewService;
import lk.chapterhouse.service.WishlistService;
import lk.chapterhouse.storage.DataLock;
import lk.chapterhouse.util.PasswordHasher;

@WebListener
public class AppBootstrap implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(AppBootstrap.class.getName());

    private static final String[] DATA_FILES = {
            "users.txt", "books.txt", "categories.txt", "reviews.txt", "wishlist.txt", "contact-messages.txt"
    };

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        try {
            Path dataDir = resolveDataDirectory();
            Files.createDirectories(dataDir);
            ensureDataFiles(dataDir);

            DataLock lock = new DataLock();
            PasswordHasher passwordHasher = new PasswordHasher();

            UserRepository userRepository = new FileUserRepository(dataDir, lock);
            BookRepository bookRepository = new FileBookRepository(dataDir, lock);
            CategoryRepository categoryRepository = new FileCategoryRepository(dataDir, lock);
            ReviewRepository reviewRepository = new FileReviewRepository(dataDir, lock);
            WishlistRepository wishlistRepository = new FileWishlistRepository(dataDir, lock);
            ContactRepository contactRepository = new FileContactRepository(dataDir, lock);

            AuthService authService = new AuthService(userRepository, passwordHasher);
            AccountService accountService = new AccountService(userRepository, reviewRepository, passwordHasher);
            CategoryService categoryService = new CategoryService(categoryRepository, bookRepository);
            BookService bookService = new BookService(bookRepository, categoryRepository, reviewRepository);
            ReviewService reviewService = new ReviewService(reviewRepository, bookRepository);
            WishlistService wishlistService = new WishlistService(wishlistRepository, bookRepository);
            ContactService contactService = new ContactService(contactRepository);

            context.setAttribute(AppAttributes.DATA_DIR, dataDir);
            context.setAttribute(AppAttributes.DATA_LOCK, lock);
            context.setAttribute(AppAttributes.PASSWORD_HASHER, passwordHasher);
            context.setAttribute(AppAttributes.USER_REPOSITORY, userRepository);
            context.setAttribute(AppAttributes.BOOK_REPOSITORY, bookRepository);
            context.setAttribute(AppAttributes.CATEGORY_REPOSITORY, categoryRepository);
            context.setAttribute(AppAttributes.REVIEW_REPOSITORY, reviewRepository);
            context.setAttribute(AppAttributes.WISHLIST_REPOSITORY, wishlistRepository);
            context.setAttribute(AppAttributes.CONTACT_REPOSITORY, contactRepository);
            context.setAttribute(AppAttributes.AUTH_SERVICE, authService);
            context.setAttribute(AppAttributes.ACCOUNT_SERVICE, accountService);
            context.setAttribute(AppAttributes.CATEGORY_SERVICE, categoryService);
            context.setAttribute(AppAttributes.BOOK_SERVICE, bookService);
            context.setAttribute(AppAttributes.REVIEW_SERVICE, reviewService);
            context.setAttribute(AppAttributes.WISHLIST_SERVICE, wishlistService);
            context.setAttribute(AppAttributes.CONTACT_SERVICE, contactService);

            if (allDataFilesEmpty(dataDir)) {
                SeedData.load(accountService, categoryService, bookService, reviewService);
            } else if (userRepository.countAdmins() == 0) {
                bootstrapAdminIfConfigured(accountService, userRepository);
            }

            LOGGER.log(Level.INFO, "Chapter House data directory: {0}", dataDir.toAbsolutePath());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to initialize application data directory.", ex);
        }
    }

    static Path resolveDataDirectory() {
        String fromProperty = System.getProperty("app.data.dir");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return Paths.get(fromProperty.trim());
        }
        String fromEnv = System.getenv("APP_DATA_DIR");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return Paths.get(fromEnv.trim());
        }
        return Paths.get(System.getProperty("user.home"), "chapter-house-data");
    }

    private static void ensureDataFiles(Path dataDir) throws IOException {
        for (String name : DATA_FILES) {
            Path file = dataDir.resolve(name);
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
        }
    }

    private static boolean allDataFilesEmpty(Path dataDir) {
        for (String name : DATA_FILES) {
            Path file = dataDir.resolve(name);
            try {
                if (Files.exists(file) && Files.size(file) > 0) {
                    return false;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Could not inspect {0}", file);
            }
        }
        return true;
    }

    private static void bootstrapAdminIfConfigured(AccountService accounts, UserRepository users) {
        String email = System.getProperty("chapterhouse.bootstrapAdminEmail");
        String password = System.getProperty("chapterhouse.bootstrapAdminPassword");
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            LOGGER.log(Level.INFO,
                    "No administrator found; set -Dchapterhouse.bootstrapAdminEmail and "
                            + "-Dchapterhouse.bootstrapAdminPassword to create one.");
            return;
        }
        if (users.findByEmail(email).isPresent()) {
            throw new IllegalStateException(
                    "Cannot create the bootstrap administrator because the configured email is already registered.");
        }
        accounts.createAdmin("Site Administrator", email, "", password.toCharArray());
        LOGGER.log(Level.INFO, "Bootstrap administrator account created for {0}", email.trim());
    }
}
