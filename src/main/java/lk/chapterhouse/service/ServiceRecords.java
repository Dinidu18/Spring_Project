package lk.chapterhouse.service;

import lk.chapterhouse.util.AppClock;

final class ServiceRecords {
    private ServiceRecords() {
    }

    static long nextVersion(long current) {
        return current + 1;
    }

    static void touchVersioned(lk.chapterhouse.model.User user) {
        user.touch(AppClock.now(), nextVersion(user.getRecordVersion()));
    }

    static void touchVersioned(lk.chapterhouse.model.Category category) {
        category.touch(AppClock.now(), nextVersion(category.getRecordVersion()));
    }

    static void touchVersioned(lk.chapterhouse.model.Book book) {
        book.touch(AppClock.now(), nextVersion(book.getRecordVersion()));
    }

    static void touchVersioned(lk.chapterhouse.model.Review review) {
        review.touch(AppClock.now(), nextVersion(review.getRecordVersion()));
    }
}
