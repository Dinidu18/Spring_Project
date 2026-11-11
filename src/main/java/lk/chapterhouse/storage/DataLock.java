package lk.chapterhouse.storage;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * One application-wide lock across all text-file repositories.
 * Read lock for reads; write lock for full validate/read/modify/write cycles.
 * Single-JVM only — not distributed locking.
 */
public final class DataLock {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public <T> T read(Supplier<T> action) {
        lock.readLock().lock();
        try {
            return action.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void read(Runnable action) {
        lock.readLock().lock();
        try {
            action.run();
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T> T write(Supplier<T> action) {
        lock.writeLock().lock();
        try {
            return action.get();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void write(Runnable action) {
        lock.writeLock().lock();
        try {
            action.run();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
