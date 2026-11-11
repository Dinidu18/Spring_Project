package lk.chapterhouse.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

public class User {
    private final String id;
    private String displayName;
    private String normalizedEmail;
    private String phone;
    private String passwordCredential;
    private Role role;
    private int sessionVersion;
    private final Instant createdAt;
    private Instant updatedAt;
    private long recordVersion;

    public User(String id, String displayName, String email, String phone, String passwordCredential,
                Role role, int sessionVersion, Instant createdAt, Instant updatedAt, long recordVersion) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("User id is required.");
        }
        this.id = id.trim();
        setDisplayName(displayName);
        setEmail(email);
        setPhone(phone);
        if (passwordCredential == null || passwordCredential.isBlank()) {
            throw new IllegalArgumentException("Password credential is required.");
        }
        this.passwordCredential = passwordCredential;
        this.role = Objects.requireNonNull(role, "Role is required.");
        this.sessionVersion = Math.max(0, sessionVersion);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.recordVersion = recordVersion;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getNormalizedEmail() { return normalizedEmail; }
    public String getPhone() { return phone; }
    public String getPasswordCredential() { return passwordCredential; }
    public Role getRole() { return role; }
    public int getSessionVersion() { return sessionVersion; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getRecordVersion() { return recordVersion; }

    public void setDisplayName(String displayName) {
        if (displayName == null || displayName.trim().length() < 2 || displayName.trim().length() > 80) {
            throw new IllegalArgumentException("Display name must be 2 to 80 characters.");
        }
        this.displayName = displayName.trim();
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") || normalized.length() > 120) {
            throw new IllegalArgumentException("Email is invalid.");
        }
        this.normalizedEmail = normalized;
    }

    public void setPhone(String phone) {
        String value = phone == null ? "" : phone.trim();
        if (!value.isEmpty() && (value.length() < 7 || value.length() > 20 || !value.matches("[0-9+\\-() ]+"))) {
            throw new IllegalArgumentException("Phone number is invalid.");
        }
        this.phone = value;
    }

    public void replacePasswordCredential(String credential) {
        if (credential == null || credential.isBlank()) {
            throw new IllegalArgumentException("Password credential is required.");
        }
        this.passwordCredential = credential;
        this.sessionVersion++;
    }

    public void touch(Instant when, long newVersion) {
        this.updatedAt = when;
        this.recordVersion = newVersion;
    }
}
