package lk.chapterhouse.repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import lk.chapterhouse.model.WishlistItem;
import lk.chapterhouse.storage.DataLock;
import lk.chapterhouse.storage.EntityCodecs;
import lk.chapterhouse.storage.StorageException;

public class FileWishlistRepository implements WishlistRepository {
    private final Path file;
    private final DataLock lock;

    public FileWishlistRepository(Path dataDir, DataLock lock) {
        this.file = dataDir.resolve("wishlist.txt");
        this.lock = lock;
    }

    @Override
    public List<WishlistItem> findAll() {
        return FileStoreSupport.loadAll(file, EntityCodecs::decodeWishlistItem, lock);
    }

    @Override
    public List<WishlistItem> findByUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return List.of();
        }
        String key = userId.trim();
        return findAll().stream().filter(w -> w.getUserId().equals(key)).toList();
    }

    @Override
    public boolean exists(String userId, String bookId) {
        if (userId == null || bookId == null) {
            return false;
        }
        String u = userId.trim();
        String b = bookId.trim();
        return findAll().stream().anyMatch(w -> w.getUserId().equals(u) && w.getBookId().equals(b));
    }

    @Override
    public WishlistItem add(WishlistItem item) {
        return lock.write(() -> {
            List<WishlistItem> items = mutableCopy(FileStoreSupport.loadAll(file, EntityCodecs::decodeWishlistItem, lock));
            boolean duplicate = items.stream()
                    .anyMatch(w -> w.getUserId().equals(item.getUserId()) && w.getBookId().equals(item.getBookId()));
            if (duplicate) {
                throw new StorageException("Wishlist entry already exists.");
            }
            items.add(item);
            persist(items);
            return item;
        });
    }

    @Override
    public void remove(String userId, String bookId) {
        lock.write(() -> {
            List<WishlistItem> items = mutableCopy(FileStoreSupport.loadAll(file, EntityCodecs::decodeWishlistItem, lock));
            boolean removed = items.removeIf(w -> w.getUserId().equals(userId) && w.getBookId().equals(bookId));
            if (!removed) {
                throw new StorageException("Wishlist entry not found.");
            }
            persist(items);
        });
    }

    private void persist(List<WishlistItem> items) {
        List<String> lines = items.stream().map(EntityCodecs::encodeWishlistItem).toList();
        FileStoreSupport.saveAll(file, lines, lock);
    }

    private static List<WishlistItem> mutableCopy(List<WishlistItem> source) {
        return new ArrayList<>(source);
    }
}
