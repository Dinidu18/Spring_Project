package lk.chapterhouse.web.util;

import jakarta.servlet.ServletContext;
import lk.chapterhouse.repository.ContactRepository;
import lk.chapterhouse.repository.ReviewRepository;
import lk.chapterhouse.repository.UserRepository;
import lk.chapterhouse.service.AccountService;
import lk.chapterhouse.service.AuthService;
import lk.chapterhouse.service.BookService;
import lk.chapterhouse.service.CategoryService;
import lk.chapterhouse.service.ContactService;
import lk.chapterhouse.service.ReviewService;
import lk.chapterhouse.service.WishlistService;
import lk.chapterhouse.web.AppAttributes;

public final class WebContext {
    private WebContext() {
    }

    public static AuthService auth(ServletContext ctx) {
        return (AuthService) ctx.getAttribute(AppAttributes.AUTH_SERVICE);
    }

    public static AccountService accounts(ServletContext ctx) {
        return (AccountService) ctx.getAttribute(AppAttributes.ACCOUNT_SERVICE);
    }

    public static BookService books(ServletContext ctx) {
        return (BookService) ctx.getAttribute(AppAttributes.BOOK_SERVICE);
    }

    public static CategoryService categories(ServletContext ctx) {
        return (CategoryService) ctx.getAttribute(AppAttributes.CATEGORY_SERVICE);
    }

    public static ReviewService reviews(ServletContext ctx) {
        return (ReviewService) ctx.getAttribute(AppAttributes.REVIEW_SERVICE);
    }

    public static WishlistService wishlist(ServletContext ctx) {
        return (WishlistService) ctx.getAttribute(AppAttributes.WISHLIST_SERVICE);
    }

    public static ContactService contact(ServletContext ctx) {
        return (ContactService) ctx.getAttribute(AppAttributes.CONTACT_SERVICE);
    }

    public static UserRepository users(ServletContext ctx) {
        return (UserRepository) ctx.getAttribute(AppAttributes.USER_REPOSITORY);
    }

    public static ReviewRepository reviewRepo(ServletContext ctx) {
        return (ReviewRepository) ctx.getAttribute(AppAttributes.REVIEW_REPOSITORY);
    }

    public static ContactRepository contactRepo(ServletContext ctx) {
        return (ContactRepository) ctx.getAttribute(AppAttributes.CONTACT_REPOSITORY);
    }
}
