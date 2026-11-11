package lk.chapterhouse.service;

import java.util.Locale;
import java.util.Optional;

import lk.chapterhouse.model.User;
import lk.chapterhouse.repository.UserRepository;
import lk.chapterhouse.util.PasswordHasher;

/**
 * Credential verification for login. Session binding is handled by the web layer;
 * successful password changes bump {@link User#getSessionVersion()} to invalidate old sessions.
 */
public class AuthService {
    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    public AuthService(UserRepository users, PasswordHasher passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    public User login(String email, char[] password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        Optional<User> user = users.findByEmail(normalized);
        if (user.isEmpty() || !passwordHasher.matches(password, user.get().getPasswordCredential())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        return user.get();
    }

    /** Conceptual logout helper: web layer clears session attributes after calling this. */
    public void logout() {
        // No server-side session store in file persistence; servlet session invalidation is enough.
    }
}
