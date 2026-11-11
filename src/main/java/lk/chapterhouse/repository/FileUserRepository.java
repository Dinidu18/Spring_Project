package lk.chapterhouse.repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import lk.chapterhouse.model.Role;
import lk.chapterhouse.model.User;
import lk.chapterhouse.storage.DataLock;
import lk.chapterhouse.storage.EntityCodecs;
import lk.chapterhouse.storage.StorageException;

public class FileUserRepository implements UserRepository {
    private final Path file;
    private final DataLock lock;

    public FileUserRepository(Path dataDir, DataLock lock) {
        this.file = dataDir.resolve("users.txt");
        this.lock = lock;
    }

    @Override
    public List<User> findAll() {
        return FileStoreSupport.loadAll(file, EntityCodecs::decodeUser, lock);
    }

    @Override
    public Optional<User> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return findAll().stream().filter(u -> u.getId().equals(id.trim())).findFirst();
    }

    @Override
    public Optional<User> findByEmail(String normalizedEmail) {
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            return Optional.empty();
        }
        String key = normalizedEmail.trim().toLowerCase(Locale.ROOT);
        return findAll().stream().filter(u -> u.getNormalizedEmail().equals(key)).findFirst();
    }

    @Override
    public long countAdmins() {
        return findAll().stream().filter(u -> u.getRole() == Role.ADMIN).count();
    }

    @Override
    public User create(User user) {
        return lock.write(() -> {
            List<User> users = mutableCopy(loadLocked());
            if (users.stream().anyMatch(u -> u.getId().equals(user.getId()))) {
                throw new StorageException("User id already exists: " + user.getId());
            }
            if (users.stream().anyMatch(u -> u.getNormalizedEmail().equals(user.getNormalizedEmail()))) {
                throw new StorageException("Email already registered: " + user.getNormalizedEmail());
            }
            users.add(user);
            persist(users);
            return user;
        });
    }

    @Override
    public User update(User user) {
        return lock.write(() -> {
            List<User> users = mutableCopy(loadLocked());
            int index = indexOf(users, user.getId());
            if (index < 0) {
                throw new StorageException("User not found: " + user.getId());
            }
            User existing = users.get(index);
            if (!existing.getNormalizedEmail().equals(user.getNormalizedEmail())
                    && users.stream().anyMatch(u -> u.getNormalizedEmail().equals(user.getNormalizedEmail()))) {
                throw new StorageException("Email already registered: " + user.getNormalizedEmail());
            }
            users.set(index, user);
            persist(users);
            return user;
        });
    }

    @Override
    public void delete(String id) {
        lock.write(() -> {
            List<User> users = mutableCopy(loadLocked());
            if (!users.removeIf(u -> u.getId().equals(id))) {
                throw new StorageException("User not found: " + id);
            }
            persist(users);
        });
    }

    private List<User> loadLocked() {
        return FileStoreSupport.loadAll(file, EntityCodecs::decodeUser, lock);
    }

    private void persist(List<User> users) {
        List<String> lines = users.stream().map(EntityCodecs::encodeUser).toList();
        FileStoreSupport.saveAll(file, lines, lock);
    }

    private static List<User> mutableCopy(List<User> source) {
        return new ArrayList<>(source);
    }

    private static int indexOf(List<User> users, String id) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
