package lk.chapterhouse.storage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lk.chapterhouse.model.Book;
import lk.chapterhouse.model.Category;
import lk.chapterhouse.model.ContactMessage;
import lk.chapterhouse.model.EBook;
import lk.chapterhouse.model.PrintedBook;
import lk.chapterhouse.model.Review;
import lk.chapterhouse.model.Role;
import lk.chapterhouse.model.User;
import lk.chapterhouse.model.WishlistItem;
import lk.chapterhouse.util.TextCodec;

/** Schema v3 pipe-delimited line codecs for UTF-8 data files. */
public final class EntityCodecs {
    public static final String SCHEMA = "v3";
    private static final String SEP = "|";

    private EntityCodecs() {
    }

    // --- Users ---

    public static String encodeUser(User user) {
        return join(
                SCHEMA, "USER", user.getId(),
                TextCodec.encode(user.getDisplayName()),
                user.getNormalizedEmail(),
                TextCodec.encode(user.getPhone()),
                user.getPasswordCredential(),
                user.getRole().name(),
                Integer.toString(user.getSessionVersion()),
                instant(user.getCreatedAt()),
                instant(user.getUpdatedAt()),
                Long.toString(user.getRecordVersion())
        );
    }

    public static User decodeUser(AtomicFileStore.StringLine line) {
        String[] p = split(line, 12);
        expectSchema(p[0], line.number());
        expectToken(p[1], "USER", line.number());
        try {
            return new User(
                    p[2],
                    TextCodec.decode(p[3]),
                    p[4],
                    TextCodec.decode(p[5]),
                    p[6],
                    Role.parse(p[7]),
                    Integer.parseInt(p[8]),
                    parseInstant(p[9], line.number()),
                    parseInstant(p[10], line.number()),
                    Long.parseLong(p[11])
            );
        } catch (RuntimeException ex) {
            throw wrap(line.number(), ex);
        }
    }

    // --- Printed books ---

    public static String encodePrintedBook(PrintedBook book) {
        return join(
                SCHEMA, PrintedBook.TYPE, book.getId(),
                TextCodec.encode(book.getTitle()),
                TextCodec.encode(book.getAuthor()),
                TextCodec.encode(book.getDescription()),
                nullToEmpty(book.getIsbn()),
                Integer.toString(book.getPublicationYear()),
                book.getCategoryId(),
                decimal(book.getPrice()),
                book.getCoverImageKey(),
                Boolean.toString(book.isFeatured()),
                instant(book.getCreatedAt()),
                instant(book.getUpdatedAt()),
                Long.toString(book.getRecordVersion()),
                Integer.toString(book.getStockQuantity()),
                Integer.toString(book.getPageCount()),
                book.getBinding()
        );
    }

    public static PrintedBook decodePrintedBook(AtomicFileStore.StringLine line) {
        String[] p = split(line, 18);
        expectSchema(p[0], line.number());
        expectToken(p[1], PrintedBook.TYPE, line.number());
        try {
            return new PrintedBook(
                    p[2],
                    TextCodec.decode(p[3]),
                    TextCodec.decode(p[4]),
                    TextCodec.decode(p[5]),
                    emptyToNull(p[6]),
                    Integer.parseInt(p[7]),
                    p[8],
                    new BigDecimal(p[9]),
                    p[10],
                    Boolean.parseBoolean(p[11]),
                    parseInstant(p[12], line.number()),
                    parseInstant(p[13], line.number()),
                    Long.parseLong(p[14]),
                    Integer.parseInt(p[15]),
                    Integer.parseInt(p[16]),
                    p[17]
            );
        } catch (RuntimeException ex) {
            throw wrap(line.number(), ex);
        }
    }

    // --- E-books ---

    public static String encodeEBook(EBook book) {
        return join(
                SCHEMA, EBook.TYPE, book.getId(),
                TextCodec.encode(book.getTitle()),
                TextCodec.encode(book.getAuthor()),
                TextCodec.encode(book.getDescription()),
                nullToEmpty(book.getIsbn()),
                Integer.toString(book.getPublicationYear()),
                book.getCategoryId(),
                decimal(book.getPrice()),
                book.getCoverImageKey(),
                Boolean.toString(book.isFeatured()),
                instant(book.getCreatedAt()),
                instant(book.getUpdatedAt()),
                Long.toString(book.getRecordVersion()),
                book.getFileFormat(),
                decimal(book.getFileSizeMb()),
                Boolean.toString(book.isDigitalAvailable())
        );
    }

    public static EBook decodeEBook(AtomicFileStore.StringLine line) {
        String[] p = split(line, 18);
        expectSchema(p[0], line.number());
        expectToken(p[1], EBook.TYPE, line.number());
        try {
            return new EBook(
                    p[2],
                    TextCodec.decode(p[3]),
                    TextCodec.decode(p[4]),
                    TextCodec.decode(p[5]),
                    emptyToNull(p[6]),
                    Integer.parseInt(p[7]),
                    p[8],
                    new BigDecimal(p[9]),
                    p[10],
                    Boolean.parseBoolean(p[11]),
                    parseInstant(p[12], line.number()),
                    parseInstant(p[13], line.number()),
                    Long.parseLong(p[14]),
                    p[15],
                    new BigDecimal(p[16]),
                    Boolean.parseBoolean(p[17])
            );
        } catch (RuntimeException ex) {
            throw wrap(line.number(), ex);
        }
    }

    public static String encodeBook(Book book) {
        if (book instanceof PrintedBook printed) {
            return encodePrintedBook(printed);
        }
        if (book instanceof EBook ebook) {
            return encodeEBook(ebook);
        }
        throw new IllegalArgumentException("Unknown book type: " + book.getClass().getName());
    }

    public static Book decodeBook(AtomicFileStore.StringLine line) {
        String text = line.text();
        if (text.isBlank()) {
            throw new StorageException("Empty book line.", line.number());
        }
        int firstSep = text.indexOf(SEP);
        int secondSep = firstSep < 0 ? -1 : text.indexOf(SEP, firstSep + 1);
        if (secondSep < 0) {
            throw new StorageException("Malformed book line.", line.number());
        }
        String type = text.substring(firstSep + 1, secondSep);
        if (PrintedBook.TYPE.equals(type)) {
            return decodePrintedBook(line);
        }
        if (EBook.TYPE.equals(type)) {
            return decodeEBook(line);
        }
        throw new StorageException("Unknown book type token: " + type, line.number());
    }

    // --- Categories ---

    public static String encodeCategory(Category category) {
        return join(
                SCHEMA, "CATEGORY", category.getId(),
                TextCodec.encode(category.getName()),
                category.getNormalizedName(),
                TextCodec.encode(category.getDescription()),
                category.getImageKey(),
                instant(category.getCreatedAt()),
                instant(category.getUpdatedAt()),
                Long.toString(category.getRecordVersion())
        );
    }

    public static Category decodeCategory(AtomicFileStore.StringLine line) {
        String[] p = split(line, 10);
        expectSchema(p[0], line.number());
        expectToken(p[1], "CATEGORY", line.number());
        try {
            return new Category(
                    p[2],
                    TextCodec.decode(p[3]),
                    TextCodec.decode(p[5]),
                    p[6],
                    parseInstant(p[7], line.number()),
                    parseInstant(p[8], line.number()),
                    Long.parseLong(p[9])
            );
        } catch (RuntimeException ex) {
            throw wrap(line.number(), ex);
        }
    }

    // --- Reviews ---

    public static String encodeReview(Review review) {
        return join(
                SCHEMA, "REVIEW", review.getId(),
                review.getBookId(),
                review.getAuthorId(),
                Integer.toString(review.getRating()),
                TextCodec.encode(review.getTitle()),
                TextCodec.encode(review.getBody()),
                instant(review.getCreatedAt()),
                instant(review.getUpdatedAt()),
                Long.toString(review.getRecordVersion())
        );
    }

    public static Review decodeReview(AtomicFileStore.StringLine line) {
        String[] p = split(line, 11);
        expectSchema(p[0], line.number());
        expectToken(p[1], "REVIEW", line.number());
        try {
            return new Review(
                    p[2],
                    p[3],
                    p[4],
                    Integer.parseInt(p[5]),
                    TextCodec.decode(p[6]),
                    TextCodec.decode(p[7]),
                    parseInstant(p[8], line.number()),
                    parseInstant(p[9], line.number()),
                    Long.parseLong(p[10])
            );
        } catch (RuntimeException ex) {
            throw wrap(line.number(), ex);
        }
    }

    // --- Wishlist ---

    public static String encodeWishlistItem(WishlistItem item) {
        return join(
                SCHEMA, "WISH",
                item.getUserId(),
                item.getBookId(),
                instant(item.getCreatedAt())
        );
    }

    public static WishlistItem decodeWishlistItem(AtomicFileStore.StringLine line) {
        String[] p = split(line, 5);
        expectSchema(p[0], line.number());
        expectToken(p[1], "WISH", line.number());
        try {
            return new WishlistItem(p[2], p[3], parseInstant(p[4], line.number()));
        } catch (RuntimeException ex) {
            throw wrap(line.number(), ex);
        }
    }

    // --- Contact messages ---

    public static String encodeContactMessage(ContactMessage message) {
        return join(
                SCHEMA, "CONTACT", message.getId(),
                message.getUserId(),
                TextCodec.encode(message.getName()),
                TextCodec.encode(message.getEmail()),
                TextCodec.encode(message.getSubject()),
                TextCodec.encode(message.getMessage()),
                instant(message.getCreatedAt())
        );
    }

    public static ContactMessage decodeContactMessage(AtomicFileStore.StringLine line) {
        String[] p = split(line, 9);
        expectSchema(p[0], line.number());
        expectToken(p[1], "CONTACT", line.number());
        try {
            return new ContactMessage(
                    p[2],
                    p[3],
                    TextCodec.decode(p[4]),
                    TextCodec.decode(p[5]),
                    TextCodec.decode(p[6]),
                    TextCodec.decode(p[7]),
                    parseInstant(p[8], line.number())
            );
        } catch (RuntimeException ex) {
            throw wrap(line.number(), ex);
        }
    }

    // --- Helpers ---

    private static String join(String... parts) {
        return String.join(SEP, parts);
    }

    private static String[] split(AtomicFileStore.StringLine line, int expectedFields) {
        String text = line.text();
        List<String> parts = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < expectedFields - 1; i++) {
            int idx = text.indexOf(SEP, start);
            if (idx < 0) {
                throw new StorageException("Expected " + expectedFields + " fields but line is too short.", line.number());
            }
            parts.add(text.substring(start, idx));
            start = idx + 1;
        }
        parts.add(text.substring(start));
        if (parts.size() != expectedFields) {
            throw new StorageException("Expected " + expectedFields + " fields.", line.number());
        }
        return parts.toArray(String[]::new);
    }

    private static void expectSchema(String token, int lineNumber) {
        if (!SCHEMA.equals(token)) {
            throw new StorageException("Unsupported schema (expected " + SCHEMA + "): " + token, lineNumber);
        }
    }

    private static void expectToken(String actual, String expected, int lineNumber) {
        if (!expected.equals(actual)) {
            throw new StorageException("Expected record type " + expected + " but found " + actual, lineNumber);
        }
    }

    private static Instant parseInstant(String value, int lineNumber) {
        try {
            return Instant.parse(value);
        } catch (RuntimeException ex) {
            throw new StorageException("Invalid ISO-8601 instant: " + value, lineNumber, ex);
        }
    }

    private static String instant(Instant instant) {
        return instant.toString();
    }

    private static String decimal(BigDecimal value) {
        return value.toPlainString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private static StorageException wrap(int lineNumber, RuntimeException ex) {
        if (ex instanceof StorageException storage) {
            if (storage.getLineNumber() == null) {
                return new StorageException(storage.getMessage(), lineNumber, storage);
            }
            return storage;
        }
        return new StorageException(ex.getMessage(), lineNumber, ex);
    }
}
