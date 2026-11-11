package lk.chapterhouse.repository;

import java.util.List;
import java.util.Optional;

import lk.chapterhouse.model.Book;

public interface BookRepository {
    List<Book> findAll();

    Optional<Book> findById(String id);

    List<Book> findByCategory(String categoryId);

    Optional<Book> findByIsbn(String isbnDigits);

    Book create(Book book);

    Book update(Book book);

    void delete(String id);
}
