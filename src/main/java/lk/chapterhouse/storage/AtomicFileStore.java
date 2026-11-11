package lk.chapterhouse.storage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AtomicFileStore {
    private static final Logger LOGGER = Logger.getLogger(AtomicFileStore.class.getName());

    private AtomicFileStore() {
    }

    public static <T> List<T> load(Path file, Function<StringLine, T> decoder) throws IOException {
        List<T> items = new ArrayList<>();
        if (!Files.exists(file) || Files.size(file) == 0) {
            return items;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            items.add(decoder.apply(new StringLine(line, lineNumber)));
        }
        return items;
    }

    public static void save(Path file, List<String> lines) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Path backup = file.resolveSibling(file.getFileName() + ".bak");
        try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException ex) {
            Files.deleteIfExists(temp);
            throw new StorageException("Failed writing temporary file for " + file.getFileName(), ex);
        }
        boolean hadOriginal = Files.exists(file);
        if (hadOriginal) {
            Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
        }
        try {
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                LOGGER.log(Level.WARNING, "Atomic move unsupported; using backup-assisted replace.", ex);
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.deleteIfExists(backup);
        } catch (IOException ex) {
            if (Files.exists(backup)) {
                try {
                    Files.move(backup, file, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException restoreEx) {
                    LOGGER.log(Level.SEVERE, "Failed restoring backup for " + file, restoreEx);
                }
            }
            Files.deleteIfExists(temp);
            throw new StorageException("Failed replacing " + file.getFileName(), ex);
        }
    }

    public record StringLine(String text, int number) {
    }
}
