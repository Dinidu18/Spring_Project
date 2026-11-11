package lk.chapterhouse.repository;

import java.util.List;

import lk.chapterhouse.model.WishlistItem;

public interface WishlistRepository {
    List<WishlistItem> findAll();

    List<WishlistItem> findByUser(String userId);

    boolean exists(String userId, String bookId);

    WishlistItem add(WishlistItem item);

    void remove(String userId, String bookId);
}
