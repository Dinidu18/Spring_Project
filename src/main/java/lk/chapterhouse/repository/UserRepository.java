package lk.chapterhouse.repository;

import java.util.List;
import java.util.Optional;

import lk.chapterhouse.model.User;

public interface UserRepository {
    List<User> findAll();

    Optional<User> findById(String id);

    Optional<User> findByEmail(String normalizedEmail);

    long countAdmins();

    User create(User user);

    User update(User user);

    void delete(String id);
}
