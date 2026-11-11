package lk.chapterhouse.storage;

public class StorageException extends RuntimeException {
    private final Integer lineNumber;

    public StorageException(String message) {
        super(message);
        this.lineNumber = null;
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
        this.lineNumber = null;
    }

    public StorageException(String message, int lineNumber) {
        super("Line " + lineNumber + ": " + message);
        this.lineNumber = lineNumber;
    }

    public StorageException(String message, int lineNumber, Throwable cause) {
        super("Line " + lineNumber + ": " + message, cause);
        this.lineNumber = lineNumber;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }
}
