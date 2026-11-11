package lk.chapterhouse.repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lk.chapterhouse.model.Book;
import lk.chapterhouse.storage.DataLock;
import lk.chapterhouse.storage.EntityCodecs;
import lk.chapterhouse.storage.StorageException;

public class FileBookRepository implements BookRepository {
    private final Path file;
    private final DataLock lock;

    public FileBookRepository(Path dataDir, DataLock lock) {
        this.file = dataDir.resolve("books.txt");
        this.lock = lock;
    }

    @Override
    public List<Book> findAll() {
        return FileStoreSupport.loadAll(file, EntityCodecs::decodeBook, lock);
    }

    @Override
    public Optional<Book> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        String key = id.trim();
        return findAll().stream().filter(b -> b.getId().equals(key)).findFirst();
    }

    @Override
    public List<Book> findByCategory(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return List.of();
        }
        String key = categoryId.trim();
        return findAll().stream().filter(b -> b.getCategoryId().equals(key)).toList();
    }

    @Override
    public Optional<Book> findByIsbn(String isbnDigits) {
        if (isbnDigits == null || isbnDigits.isBlank()) {
            return Optional.empty();
        }
        String digits = isbnDigits.trim().replace("-", "").replace(" ", "");
        return findAll().stream()
                .filter(b -> !b.getIsbn().isEmpty() && b.getIsbn().equals(digits))
                .findFirst();
    }

    @Override
    public Book create(Book book) {
        return lock.write(() -> {
            List<Book> books = mutableCopy(FileStoreSupport.loadAll(file, EntityCodecs::decodeBook, lock));
            if (books.stream().anyMatch(b -> b.getId().equals(book.getId()))) {
                throw new StorageException("Book id already exists: " + book.getId());
            }
            if (!book.getIsbn().isEmpty() && books.stream().anyMatch(b -> book.getIsbn().equals(b.getIsbn()))) {
                throw new StorageException("ISBN already exists: " + book.getIsbn());
            }
            books.add(book);
            persist(books);
            return book;
        });
    }

    @Override
    public Book update(Book book) {
        return lock.write(() -> {
            List<Book> books = mutableCopy(FileStoreSupport.loadAll(file, EntityCodecs::decodeBook, lock));
            int index = indexOf(books, book.getId());
            if (index < 0) {
                throw new StorageException("Book not found: " + book.getId());
            }
            if (!book.getIsbn().isEmpty()) {
                boolean clash = books.stream()
                        .anyMatch(b -> !b.getId().equals(book.getId()) && book.getIsbn().equals(b.getIsbn()));
                if (clash) {
                    throw new StorageException("ISBN already exists: " + book.getIsbn());
                }
            }
            books.set(index, book);
            persist(books);
            return book;
        });
    }

    @Override
    public void delete(String id) {
        lock.write(() -> {
            List<Book> books = mutableCopy(FileStoreSupport.loadAll(file, EntityCodecs::decodeBook, lock));
            if (!books.removeIf(b -> b.getId().equals(id))) {
                throw new StorageException("Book not found: " + id);
            }
            persist(books);
        });
    }

    private void persist(List<Book> books) {
        List<String> lines = books.stream().map(EntityCodecs::encodeBook).toList();
        FileStoreSupport.saveAll(file, lines, lock);
    }

    private static List<Book> mutableCopy(List<Book> source) {
        return new ArrayList<>(source);
    }

    private static int indexOf(List<Book> books, String id) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
