package lk.chapterhouse.web;

/** ServletContext attribute keys for Chapter House wiring. */
public final class AppAttributes {
    public static final String DATA_DIR = "chapterhouse.dataDir";
    public static final String DATA_LOCK = "chapterhouse.dataLock";
    public static final String PASSWORD_HASHER = "chapterhouse.passwordHasher";

    public static final String USER_REPOSITORY = "chapterhouse.userRepository";
    public static final String BOOK_REPOSITORY = "chapterhouse.bookRepository";
    public static final String CATEGORY_REPOSITORY = "chapterhouse.categoryRepository";
    public static final String REVIEW_REPOSITORY = "chapterhouse.reviewRepository";
    public static final String WISHLIST_REPOSITORY = "chapterhouse.wishlistRepository";
    public static final String CONTACT_REPOSITORY = "chapterhouse.contactRepository";

    public static final String AUTH_SERVICE = "chapterhouse.authService";
    public static final String ACCOUNT_SERVICE = "chapterhouse.accountService";
    public static final String CATEGORY_SERVICE = "chapterhouse.categoryService";
    public static final String BOOK_SERVICE = "chapterhouse.bookService";
    public static final String REVIEW_SERVICE = "chapterhouse.reviewService";
    public static final String WISHLIST_SERVICE = "chapterhouse.wishlistService";
    public static final String CONTACT_SERVICE = "chapterhouse.contactService";

    private AppAttributes() {
    }
}
