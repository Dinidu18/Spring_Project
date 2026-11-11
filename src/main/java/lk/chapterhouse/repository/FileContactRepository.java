package lk.chapterhouse.repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lk.chapterhouse.model.ContactMessage;
import lk.chapterhouse.storage.DataLock;
import lk.chapterhouse.storage.EntityCodecs;
import lk.chapterhouse.storage.StorageException;

public class FileContactRepository implements ContactRepository {
    private final Path file;
    private final DataLock lock;

    public FileContactRepository(Path dataDir, DataLock lock) {
        this.file = dataDir.resolve("contact-messages.txt");
        this.lock = lock;
    }

    @Override
    public List<ContactMessage> findAll() {
        return FileStoreSupport.loadAll(file, EntityCodecs::decodeContactMessage, lock);
    }

    @Override
    public Optional<ContactMessage> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        String key = id.trim();
        return findAll().stream().filter(m -> m.getId().equals(key)).findFirst();
    }

    @Override
    public ContactMessage create(ContactMessage message) {
        return lock.write(() -> {
            List<ContactMessage> messages = mutableCopy(
                    FileStoreSupport.loadAll(file, EntityCodecs::decodeContactMessage, lock));
            if (messages.stream().anyMatch(m -> m.getId().equals(message.getId()))) {
                throw new StorageException("Contact message id already exists: " + message.getId());
            }
            messages.add(message);
            persist(messages);
            return message;
        });
    }

    private void persist(List<ContactMessage> messages) {
        List<String> lines = messages.stream().map(EntityCodecs::encodeContactMessage).toList();
        FileStoreSupport.saveAll(file, lines, lock);
    }

    private static List<ContactMessage> mutableCopy(List<ContactMessage> source) {
        return new ArrayList<>(source);
    }
}
