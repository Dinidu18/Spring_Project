package lk.chapterhouse.model;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

public class ContactMessage {
    private final String id;
    private final String userId;
    private final String name;
    private final String email;
    private final String subject;
    private final String message;
    private final Instant createdAt;

    public ContactMessage(String id, String userId, String name, String email, String subject,
                          String message, Instant createdAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Message id is required.");
        }
        if (name == null || name.trim().length() < 2 || name.trim().length() > 80) {
            throw new IllegalArgumentException("Name must be 2 to 80 characters.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (subject == null || subject.trim().length() < 3 || subject.trim().length() > 120) {
            throw new IllegalArgumentException("Subject must be 3 to 120 characters.");
        }
        if (message == null || message.trim().length() < 10 || message.trim().length() > 3000) {
            throw new IllegalArgumentException("Message must be 10 to 3000 characters.");
        }
        this.id = id.trim();
        this.userId = userId == null ? "" : userId.trim();
        this.name = name.trim();
        this.email = email.trim();
        this.subject = subject.trim();
        this.message = message.trim();
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }

    /** For JSTL fmt:formatDate (expects java.util.Date). */
    public Date getCreatedAtDate() {
        return Date.from(createdAt);
    }
}
