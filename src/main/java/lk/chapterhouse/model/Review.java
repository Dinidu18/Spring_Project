package lk.chapterhouse.model;

import java.time.Instant;
import java.util.Objects;

public class Review {
    private final String id;
    private final String bookId;
    private final String authorId;
    private int rating;
    private String title;
    private String body;
    private final Instant createdAt;
    private Instant updatedAt;
    private long recordVersion;

    public Review(String id, String bookId, String authorId, int rating, String title, String body,
                  Instant createdAt, Instant updatedAt, long recordVersion) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Review id is required.");
        }
        if (bookId == null || bookId.isBlank()) {
            throw new IllegalArgumentException("Book id is required.");
        }
        if (authorId == null || authorId.isBlank()) {
            throw new IllegalArgumentException("Author id is required.");
        }
        this.id = id.trim();
        this.bookId = bookId.trim();
        this.authorId = authorId.trim();
        setRating(rating);
        setTitle(title);
        setBody(body);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.recordVersion = recordVersion;
    }

    public String getId() { return id; }
    public String getBookId() { return bookId; }
    public String getAuthorId() { return authorId; }
    public int getRating() { return rating; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getRecordVersion() { return recordVersion; }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be an integer from 1 to 5.");
        }
        this.rating = rating;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().length() < 3 || title.trim().length() > 100) {
            throw new IllegalArgumentException("Review title must be 3 to 100 characters.");
        }
        this.title = title.trim();
    }

    public void setBody(String body) {
        if (body == null || body.trim().length() < 10 || body.trim().length() > 3000) {
            throw new IllegalArgumentException("Review body must be 10 to 3000 characters.");
        }
        this.body = body.trim();
    }

    public void touch(Instant when, long newVersion) {
        this.updatedAt = when;
        this.recordVersion = newVersion;
    }
}
