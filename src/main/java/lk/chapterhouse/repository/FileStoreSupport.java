package lk.chapterhouse.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import lk.chapterhouse.storage.AtomicFileStore;
import lk.chapterhouse.storage.DataLock;
import lk.chapterhouse.storage.StorageException;

final class FileStoreSupport {
    private FileStoreSupport() {
    }

    static <T> List<T> loadAll(Path file, Function<AtomicFileStore.StringLine, T> decoder, DataLock lock) {
        return lock.read(() -> {
            try {
                return List.copyOf(AtomicFileStore.load(file, decoder));
            } catch (StorageException ex) {
                throw ex;
            } catch (IOException ex) {
                throw new StorageException("Failed reading " + file.getFileName(), ex);
            }
        });
    }

    static void saveAll(Path file, List<String> lines, DataLock lock) {
        lock.write(() -> {
            try {
                AtomicFileStore.save(file, lines);
            } catch (IOException ex) {
                throw new StorageException("Failed writing " + file.getFileName(), ex);
            }
        });
    }

    static boolean isFileEmpty(Path file) {
        try {
            return !Files.exists(file) || Files.size(file) == 0;
        } catch (IOException ex) {
            return true;
        }
    }

    static List<String> encodeAll(List<?> items, Function<Object, String> encoder) {
        List<String> lines = new ArrayList<>(items.size());
        for (Object item : items) {
            lines.add(encoder.apply(item));
        }
        return lines;
    }
}
