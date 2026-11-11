package lk.chapterhouse.service;

import lk.chapterhouse.model.Role;
import lk.chapterhouse.model.User;
import lk.chapterhouse.repository.ReviewRepository;
import lk.chapterhouse.repository.UserRepository;
import lk.chapterhouse.util.AppClock;
import lk.chapterhouse.util.Ids;
import lk.chapterhouse.util.PasswordHasher;

public class AccountService {
    private final UserRepository users;
    private final ReviewRepository reviews;
    private final PasswordHasher passwordHasher;

    public AccountService(UserRepository users, ReviewRepository reviews, PasswordHasher passwordHasher) {
        this.users = users;
        this.reviews = reviews;
        this.passwordHasher = passwordHasher;
    }

    public User registerMember(String displayName, String email, String phone, char[] password) {
        if (users.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        var now = AppClock.now();
        User user = new User(
                Ids.newId(),
                displayName,
                email,
                phone,
                passwordHasher.hash(password),
                Role.MEMBER,
                0,
                now,
                now,
                1L
        );
        return users.create(user);
    }

    public User createAdmin(String displayName, String email, String phone, char[] password) {
        var now = AppClock.now();
        User user = new User(
                Ids.newId(),
                displayName,
                email,
                phone,
                passwordHasher.hash(password),
                Role.ADMIN,
                0,
                now,
                now,
                1L
        );
        return users.create(user);
    }

    public User updateProfile(String userId, String displayName, String email, String phone) {
        User user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        users.findByEmail(email).ifPresent(other -> {
            if (!other.getId().equals(userId)) {
                throw new IllegalArgumentException("Email is already in use.");
            }
        });
        user.setDisplayName(displayName);
        user.setEmail(email);
        user.setPhone(phone);
        ServiceRecords.touchVersioned(user);
        return users.update(user);
    }

    public User changePassword(String userId, char[] currentPassword, char[] newPassword) {
        User user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        if (!passwordHasher.matches(currentPassword, user.getPasswordCredential())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        user.replacePasswordCredential(passwordHasher.hash(newPassword));
        ServiceRecords.touchVersioned(user);
        return users.update(user);
    }

    public void deleteAccount(String userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        if (user.getRole() == Role.ADMIN && users.countAdmins() <= 1) {
            throw new IllegalStateException("Cannot delete the last administrator account.");
        }
        if (!reviews.findByAuthor(userId).isEmpty()) {
            throw new IllegalStateException("Remove your reviews before deleting the account.");
        }
        users.delete(userId);
    }
}
