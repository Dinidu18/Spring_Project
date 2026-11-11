package lk.chapterhouse.web.dto;

import java.time.Instant;
import java.util.Date;

import lk.chapterhouse.model.Role;
import lk.chapterhouse.model.User;

public final class UserView {
    private final String id;
    private final String displayName;
    private final String email;
    private final String phone;
    private final Role role;
    private final Instant createdAt;
    private final Instant updatedAt;

    private UserView(String id, String displayName, String email, String phone, Role role,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserView from(User user) {
        return new UserView(
                user.getId(),
                user.getDisplayName(),
                user.getNormalizedEmail(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Role getRole() { return role; }
    public boolean isAdmin() { return role == Role.ADMIN; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Date getCreatedAtDate() { return Date.from(createdAt); }
    public Date getUpdatedAtDate() { return Date.from(updatedAt); }
}
