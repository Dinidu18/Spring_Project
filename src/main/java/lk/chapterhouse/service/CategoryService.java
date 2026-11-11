package lk.chapterhouse.service;

import java.util.List;

import lk.chapterhouse.model.Category;
import lk.chapterhouse.repository.BookRepository;
import lk.chapterhouse.repository.CategoryRepository;
import lk.chapterhouse.util.AppClock;
import lk.chapterhouse.util.Ids;

public class CategoryService {
    private final CategoryRepository categories;
    private final BookRepository books;

    public CategoryService(CategoryRepository categories, BookRepository books) {
        this.categories = categories;
        this.books = books;
    }

    public List<Category> listAll() {
        return categories.findAll();
    }

    public Category getById(String id) {
        return categories.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));
    }

    public Category create(String name, String description, String imageKey) {
        if (categories.findByNormalizedName(name).isPresent()) {
            throw new IllegalArgumentException("Category name already exists.");
        }
        var now = AppClock.now();
        Category category = new Category(Ids.newId(), name, description, imageKey, now, now, 1L);
        return categories.create(category);
    }

    public Category update(String id, String name, String description, String imageKey) {
        Category category = getById(id);
        categories.findByNormalizedName(name).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new IllegalArgumentException("Category name already exists.");
            }
        });
        category.setName(name);
        category.setDescription(description);
        category.setImageKey(imageKey);
        ServiceRecords.touchVersioned(category);
        return categories.update(category);
    }

    public void delete(String id) {
        getById(id);
        if (!books.findByCategory(id).isEmpty()) {
            throw new IllegalStateException("Remove or reassign books before deleting this category.");
        }
        categories.delete(id);
    }
}
