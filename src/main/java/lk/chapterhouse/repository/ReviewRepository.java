package lk.chapterhouse.repository;

import java.util.List;
import java.util.Optional;

import lk.chapterhouse.model.Review;

public interface ReviewRepository {
    List<Review> findAll();

    Optional<Review> findById(String id);

    List<Review> findByBook(String bookId);

    List<Review> findByAuthor(String authorId);

    Optional<Review> findByBookAndAuthor(String bookId, String authorId);

    Review create(Review review);

    Review update(Review review);

    void delete(String id);
}
