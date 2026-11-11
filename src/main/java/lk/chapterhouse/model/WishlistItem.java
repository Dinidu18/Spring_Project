package lk.chapterhouse.model;

import java.time.Instant;
import java.util.Objects;

public class WishlistItem {
    private final String userId;
    private final String bookId;
    private final Instant createdAt;

    public WishlistItem(String userId, String bookId, Instant createdAt) {
        if (userId == null || userId.isBlank() || bookId == null || bookId.isBlank()) {
            throw new IllegalArgumentException("Wishlist user and book are required.");
        }
        this.userId = userId.trim();
        this.bookId = bookId.trim();
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public String getUserId() { return userId; }
    public String getBookId() { return bookId; }
    public Instant getCreatedAt() { return createdAt; }
}
