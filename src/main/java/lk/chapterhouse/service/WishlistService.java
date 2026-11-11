package lk.chapterhouse.service;

import java.util.ArrayList;
import java.util.List;

import lk.chapterhouse.model.WishlistItem;
import lk.chapterhouse.repository.BookRepository;
import lk.chapterhouse.repository.WishlistRepository;
import lk.chapterhouse.util.AppClock;

public class WishlistService {
    private final WishlistRepository wishlist;
    private final BookRepository books;

    public WishlistService(WishlistRepository wishlist, BookRepository books) {
        this.wishlist = wishlist;
        this.books = books;
    }

    public List<WishlistItem> listForUser(String userId) {
        return pruneMissingBooks(wishlist.findByUser(userId));
    }

    public WishlistItem add(String userId, String bookId) {
        books.findById(bookId).orElseThrow(() -> new IllegalArgumentException("Book not found."));
        if (wishlist.exists(userId, bookId)) {
            throw new IllegalArgumentException("Book is already on your wishlist.");
        }
        WishlistItem item = new WishlistItem(userId, bookId, AppClock.now());
        return wishlist.add(item);
    }

    public void remove(String userId, String bookId) {
        wishlist.remove(userId, bookId);
    }

    /** Drops entries whose book records no longer exist. */
    public int pruneMissingBooksForUser(String userId) {
        List<WishlistItem> items = new ArrayList<>(wishlist.findByUser(userId));
        int removed = 0;
        for (WishlistItem item : items) {
            if (books.findById(item.getBookId()).isEmpty()) {
                wishlist.remove(item.getUserId(), item.getBookId());
                removed++;
            }
        }
        return removed;
    }

    private List<WishlistItem> pruneMissingBooks(List<WishlistItem> items) {
        List<WishlistItem> valid = new ArrayList<>();
        for (WishlistItem item : items) {
            if (books.findById(item.getBookId()).isPresent()) {
                valid.add(item);
            }
        }
        return List.copyOf(valid);
    }
}
