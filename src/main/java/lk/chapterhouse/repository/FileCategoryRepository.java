package lk.chapterhouse.repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import lk.chapterhouse.model.Category;
import lk.chapterhouse.storage.DataLock;
import lk.chapterhouse.storage.EntityCodecs;
import lk.chapterhouse.storage.StorageException;

public class FileCategoryRepository implements CategoryRepository {
    private final Path file;
    private final DataLock lock;

    public FileCategoryRepository(Path dataDir, DataLock lock) {
        this.file = dataDir.resolve("categories.txt");
        this.lock = lock;
    }

    @Override
    public List<Category> findAll() {
        return FileStoreSupport.loadAll(file, EntityCodecs::decodeCategory, lock);
    }

    @Override
    public Optional<Category> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        String key = id.trim();
        return findAll().stream().filter(c -> c.getId().equals(key)).findFirst();
    }

    @Override
    public Optional<Category> findByNormalizedName(String normalizedName) {
        if (normalizedName == null || normalizedName.isBlank()) {
            return Optional.empty();
        }
        String key = normalizedName.trim().toLowerCase(Locale.ROOT);
        return findAll().stream().filter(c -> c.getNormalizedName().equals(key)).findFirst();
    }

    @Override
    public Category create(Category category) {
        return lock.write(() -> {
            List<Category> categories = mutableCopy(FileStoreSupport.loadAll(file, EntityCodecs::decodeCategory, lock));
            if (categories.stream().anyMatch(c -> c.getId().equals(category.getId()))) {
                throw new StorageException("Category id already exists: " + category.getId());
            }
            if (categories.stream().anyMatch(c -> c.getNormalizedName().equals(category.getNormalizedName()))) {
                throw new StorageException("Category name already exists: " + category.getName());
            }
            categories.add(category);
            persist(categories);
            return category;
        });
    }

    @Override
    public Category update(Category category) {
        return lock.write(() -> {
            List<Category> categories = mutableCopy(FileStoreSupport.loadAll(file, EntityCodecs::decodeCategory, lock));
            int index = indexOf(categories, category.getId());
            if (index < 0) {
                throw new StorageException("Category not found: " + category.getId());
            }
            boolean nameClash = categories.stream()
                    .anyMatch(c -> !c.getId().equals(category.getId())
                            && c.getNormalizedName().equals(category.getNormalizedName()));
            if (nameClash) {
                throw new StorageException("Category name already exists: " + category.getName());
            }
            categories.set(index, category);
            persist(categories);
            return category;
        });
    }

    @Override
    public void delete(String id) {
        lock.write(() -> {
            List<Category> categories = mutableCopy(FileStoreSupport.loadAll(file, EntityCodecs::decodeCategory, lock));
            if (!categories.removeIf(c -> c.getId().equals(id))) {
                throw new StorageException("Category not found: " + id);
            }
            persist(categories);
        });
    }

    private void persist(List<Category> categories) {
        List<String> lines = categories.stream().map(EntityCodecs::encodeCategory).toList();
        FileStoreSupport.saveAll(file, lines, lock);
    }

    private static List<Category> mutableCopy(List<Category> source) {
        return new ArrayList<>(source);
    }

    private static int indexOf(List<Category> categories, String id) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
