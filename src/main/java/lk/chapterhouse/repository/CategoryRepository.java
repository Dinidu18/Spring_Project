package lk.chapterhouse.repository;

import java.util.List;
import java.util.Optional;

import lk.chapterhouse.model.Category;

public interface CategoryRepository {
    List<Category> findAll();

    Optional<Category> findById(String id);

    Optional<Category> findByNormalizedName(String normalizedName);

    Category create(Category category);

    Category update(Category category);

    void delete(String id);
}
