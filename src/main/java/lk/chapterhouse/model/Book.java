package lk.chapterhouse.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

public abstract class Book {
    private final String id;
    private String title;
    private String author;
    private String description;
    private String isbn;
    private int publicationYear;
    private String categoryId;
    private BigDecimal price;
    private String coverImageKey;
    private boolean featured;
    private final Instant createdAt;
    private Instant updatedAt;
    private long recordVersion;

    protected Book(String id, String title, String author, String description, String isbn,
                   int publicationYear, String categoryId, BigDecimal price, String coverImageKey,
                   boolean featured, Instant createdAt, Instant updatedAt, long recordVersion) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Book id is required.");
        }
        this.id = id.trim();
        applyCommon(title, author, description, isbn, publicationYear, categoryId, price, coverImageKey, featured);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.recordVersion = recordVersion;
    }

    public final void applyCommon(String title, String author, String description, String isbn,
                                  int publicationYear, String categoryId, BigDecimal price,
                                  String coverImageKey, boolean featured) {
        setTitle(title);
        setAuthor(author);
        setDescription(description);
        setIsbn(isbn);
        setPublicationYear(publicationYear);
        setCategoryId(categoryId);
        setPrice(price);
        setCoverImageKey(coverImageKey);
        this.featured = featured;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public String getIsbn() { return isbn; }
    public int getPublicationYear() { return publicationYear; }
    public String getCategoryId() { return categoryId; }
    public BigDecimal getPrice() { return price; }
    public String getCoverImageKey() { return coverImageKey; }
    public boolean isFeatured() { return featured; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getRecordVersion() { return recordVersion; }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty() || title.trim().length() > 180) {
            throw new IllegalArgumentException("Title must be 1 to 180 characters.");
        }
        this.title = title.trim();
    }

    public void setAuthor(String author) {
        if (author == null || author.trim().isEmpty() || author.trim().length() > 120) {
            throw new IllegalArgumentException("Author must be 1 to 120 characters.");
        }
        this.author = author.trim();
    }

    public void setDescription(String description) {
        if (description == null || description.trim().length() < 30 || description.trim().length() > 5000) {
            throw new IllegalArgumentException("Description must be 30 to 5000 characters.");
        }
        this.description = description.trim();
    }

    public void setIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            this.isbn = "";
            return;
        }
        String digits = isbn.trim().replace("-", "").replace(" ", "");
        if (!(digits.matches("\\d{13}") || digits.matches("\\d{10}")) || !IsbnUtil.isValid(digits)) {
            throw new IllegalArgumentException("ISBN is invalid.");
        }
        this.isbn = digits;
    }

    public void setPublicationYear(int publicationYear) {
        if (publicationYear < 1450 || publicationYear > 2100) {
            throw new IllegalArgumentException("Publication year is out of range.");
        }
        this.publicationYear = publicationYear;
    }

    public void setCategoryId(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }
        this.categoryId = categoryId.trim();
    }

    public void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (price.scale() > 2) {
            throw new IllegalArgumentException("Price may have at most two decimal places.");
        }
        this.price = price.setScale(2, RoundingMode.UNNECESSARY);
    }

    public void setCoverImageKey(String coverImageKey) {
        if (coverImageKey == null || coverImageKey.isBlank()) {
            throw new IllegalArgumentException("Cover image key is required.");
        }
        this.coverImageKey = coverImageKey.trim();
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public void touch(Instant when, long newVersion) {
        this.updatedAt = when;
        this.recordVersion = newVersion;
    }

    public abstract String getTypeCode();
    public abstract String getFormatDetails();
    public abstract boolean isAvailable();
}
