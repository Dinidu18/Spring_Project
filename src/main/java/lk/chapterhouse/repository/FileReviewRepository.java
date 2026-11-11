package lk.chapterhouse.repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lk.chapterhouse.model.Review;
import lk.chapterhouse.storage.DataLock;
import lk.chapterhouse.storage.EntityCodecs;
import lk.chapterhouse.storage.StorageException;

public class FileReviewRepository implements ReviewRepository {
    private final Path file;
    private final DataLock lock;

    public FileReviewRepository(Path dataDir, DataLock lock) {
        this.file = dataDir.resolve("reviews.txt");
        this.lock = lock;
    }

    @Override
    public List<Review> findAll() {
        return FileStoreSupport.loadAll(file, EntityCodecs::decodeReview, lock);
    }

    @Override
    public Optional<Review> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        String key = id.trim();
        return findAll().stream().filter(r -> r.getId().equals(key)).findFirst();
    }

    @Override
    public List<Review> findByBook(String bookId) {
        if (bookId == null || bookId.isBlank()) {
            return List.of();
        }
        String key = bookId.trim();
        return findAll().stream().filter(r -> r.getBookId().equals(key)).toList();
    }

    @Override
    public List<Review> findByAuthor(String authorId) {
        if (authorId == null || authorId.isBlank()) {
            return List.of();
        }
        String key = authorId.trim();
        return findAll().stream().filter(r -> r.getAuthorId().equals(key)).toList();
    }

    @Override
    public Optional<Review> findByBookAndAuthor(String bookId, String authorId) {
        if (bookId == null || bookId.isBlank() || authorId == null || authorId.isBlank()) {
            return Optional.empty();
        }
        String b = bookId.trim();
        String a = authorId.trim();
        return findAll().stream().filter(r -> r.getBookId().equals(b) && r.getAuthorId().equals(a)).findFirst();
    }

    @Override
    public Review create(Review review) {
        return lock.write(() -> {
            List<Review> reviews = mutableCopy(FileStoreSupport.loadAll(file, EntityCodecs::decodeReview, lock));
            if (reviews.stream().anyMatch(r -> r.getId().equals(review.getId()))) {
                throw new StorageException("Review id already exists: " + review.getId());
            }
            reviews.add(review);
            persist(reviews);
            return review;
        });
    }

    @Override
    public Review update(Review review) {
        return lock.write(() -> {
            List<Review> reviews = mutableCopy(FileStoreSupport.loadAll(file, EntityCodecs::decodeReview, lock));
            int index = indexOf(reviews, review.getId());
            if (index < 0) {
                throw new StorageException("Review not found: " + review.getId());
            }
            reviews.set(index, review);
            persist(reviews);
            return review;
        });
    }

    @Override
    public void delete(String id) {
        lock.write(() -> {
            List<Review> reviews = mutableCopy(FileStoreSupport.loadAll(file, EntityCodecs::decodeReview, lock));
            if (!reviews.removeIf(r -> r.getId().equals(id))) {
                throw new StorageException("Review not found: " + id);
            }
            persist(reviews);
        });
    }

    private void persist(List<Review> reviews) {
        List<String> lines = reviews.stream().map(EntityCodecs::encodeReview).toList();
        FileStoreSupport.saveAll(file, lines, lock);
    }

    private static List<Review> mutableCopy(List<Review> source) {
        return new ArrayList<>(source);
    }

    private static int indexOf(List<Review> reviews, String id) {
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
