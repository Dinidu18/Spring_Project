package lk.chapterhouse.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import lk.chapterhouse.model.Book;
import lk.chapterhouse.model.EBook;
import lk.chapterhouse.model.PrintedBook;
import lk.chapterhouse.repository.BookRepository;
import lk.chapterhouse.repository.CategoryRepository;
import lk.chapterhouse.repository.ReviewRepository;
import lk.chapterhouse.util.AppClock;
import lk.chapterhouse.util.Ids;

public class BookService {
    private final BookRepository books;
    private final CategoryRepository categories;
    private final ReviewRepository reviews;

    public BookService(BookRepository books, CategoryRepository categories, ReviewRepository reviews) {
        this.books = books;
        this.categories = categories;
        this.reviews = reviews;
    }

    public List<Book> listAll() {
        return books.findAll();
    }

    /** Books currently in stock / digitally available, sorted by title. */
    public List<Book> listAvailable() {
        List<Book> available = new ArrayList<>();
        for (Book book : books.findAll()) {
            if (book.isAvailable()) {
                available.add(book);
            }
        }
        available.sort(Comparator.comparing(b -> b.getTitle().toLowerCase(Locale.ROOT)));
        return available;
    }

    public Book getById(String id) {
        return books.findById(id).orElseThrow(() -> new IllegalArgumentException("Book not found."));
    }

    public PrintedBook createPrinted(String title, String author, String description, String isbn,
                                     int publicationYear, String categoryId, BigDecimal price,
                                     String coverImageKey, boolean featured, int stockQuantity,
                                     int pageCount, String binding) {
        ensureCategory(categoryId);
        ensureIsbnAvailable(isbn, null);
        var now = AppClock.now();
        PrintedBook book = new PrintedBook(
                Ids.newId(), title, author, description, isbn, publicationYear, categoryId, price,
                coverImageKey, featured, now, now, 1L, stockQuantity, pageCount, binding);
        return (PrintedBook) books.create(book);
    }

    public EBook createEbook(String title, String author, String description, String isbn,
                             int publicationYear, String categoryId, BigDecimal price,
                             String coverImageKey, boolean featured, String fileFormat,
                             BigDecimal fileSizeMb, boolean digitalAvailable) {
        ensureCategory(categoryId);
        ensureIsbnAvailable(isbn, null);
        var now = AppClock.now();
        EBook book = new EBook(
                Ids.newId(), title, author, description, isbn, publicationYear, categoryId, price,
                coverImageKey, featured, now, now, 1L, fileFormat, fileSizeMb, digitalAvailable);
        return (EBook) books.create(book);
    }

    public Book updateCommon(String id, String title, String author, String description, String isbn,
                             int publicationYear, String categoryId, BigDecimal price,
                             String coverImageKey, boolean featured) {
        Book book = getById(id);
        ensureCategory(categoryId);
        ensureIsbnAvailable(isbn, id);
        book.applyCommon(title, author, description, isbn, publicationYear, categoryId, price, coverImageKey, featured);
        ServiceRecords.touchVersioned(book);
        return books.update(book);
    }

    public PrintedBook updatePrinted(String id, String title, String author, String description, String isbn,
                                     int publicationYear, String categoryId, BigDecimal price,
                                     String coverImageKey, boolean featured, int stockQuantity,
                                     int pageCount, String binding) {
        Book book = getById(id);
        if (!(book instanceof PrintedBook printed)) {
            throw new IllegalStateException("Cannot change book subtype from e-book to printed.");
        }
        ensureCategory(categoryId);
        ensureIsbnAvailable(isbn, id);
        printed.applyCommon(title, author, description, isbn, publicationYear, categoryId, price, coverImageKey, featured);
        printed.setStockQuantity(stockQuantity);
        printed.setPageCount(pageCount);
        printed.setBinding(binding);
        ServiceRecords.touchVersioned(printed);
        return (PrintedBook) books.update(printed);
    }

    public EBook updateEbook(String id, String title, String author, String description, String isbn,
                             int publicationYear, String categoryId, BigDecimal price,
                             String coverImageKey, boolean featured, String fileFormat,
                             BigDecimal fileSizeMb, boolean digitalAvailable) {
        Book book = getById(id);
        if (!(book instanceof EBook ebook)) {
            throw new IllegalStateException("Cannot change book subtype from printed to e-book.");
        }
        ensureCategory(categoryId);
        ensureIsbnAvailable(isbn, id);
        ebook.applyCommon(title, author, description, isbn, publicationYear, categoryId, price, coverImageKey, featured);
        ebook.setFileFormat(fileFormat);
        ebook.setFileSizeMb(fileSizeMb);
        ebook.setDigitalAvailable(digitalAvailable);
        ServiceRecords.touchVersioned(ebook);
        return (EBook) books.update(ebook);
    }

    public void delete(String id) {
        getById(id);
        if (!reviews.findByBook(id).isEmpty()) {
            throw new IllegalStateException("Remove reviews before deleting this book.");
        }
        books.delete(id);
    }

    public CatalogPage search(CatalogQuery query) {
        List<Book> filtered = new ArrayList<>();
        String needle = query.searchText() == null ? "" : query.searchText().trim().toLowerCase(Locale.ROOT);
        for (Book book : books.findAll()) {
            if (query.categoryId() != null && !query.categoryId().isBlank()
                    && !book.getCategoryId().equals(query.categoryId().trim())) {
                continue;
            }
            if (query.bookType() != null && !query.bookType().isBlank()
                    && !book.getTypeCode().equalsIgnoreCase(query.bookType().trim())) {
                continue;
            }
            if (Boolean.TRUE.equals(query.featuredOnly()) && !book.isFeatured()) {
                continue;
            }
            if (!needle.isEmpty()) {
                String hay = (book.getTitle() + " " + book.getAuthor() + " " + book.getDescription())
                        .toLowerCase(Locale.ROOT);
                if (!hay.contains(needle)) {
                    continue;
                }
            }
            filtered.add(book);
        }
        Comparator<Book> comparator = comparatorFor(query.sortField());
        if (query.sortDescending()) {
            comparator = comparator.reversed();
        }
        filtered.sort(comparator);
        int total = filtered.size();
        int totalPages = total == 0 ? 0 : (int) Math.ceil(total / (double) query.pageSize());
        int from = Math.min((query.page() - 1) * query.pageSize(), total);
        int to = Math.min(from + query.pageSize(), total);
        List<Book> pageItems = filtered.subList(from, to);
        return new CatalogPage(pageItems, query.page(), query.pageSize(), total, totalPages);
    }

    private void ensureCategory(String categoryId) {
        categories.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));
    }

    private void ensureIsbnAvailable(String isbn, String exceptBookId) {
        if (isbn == null || isbn.isBlank()) {
            return;
        }
        books.findByIsbn(isbn).ifPresent(existing -> {
            if (exceptBookId == null || !existing.getId().equals(exceptBookId)) {
                throw new IllegalArgumentException("ISBN is already assigned to another book.");
            }
        });
    }

    private static Comparator<Book> comparatorFor(CatalogQuery.SortField field) {
        return switch (field) {
            case AUTHOR -> Comparator.comparing(b -> b.getAuthor().toLowerCase(Locale.ROOT));
            case PRICE -> Comparator.comparing(Book::getPrice);
            case YEAR -> Comparator.comparingInt(Book::getPublicationYear);
            case CREATED -> Comparator.comparing(Book::getCreatedAt);
            case TITLE -> Comparator.comparing(b -> b.getTitle().toLowerCase(Locale.ROOT));
        };
    }
}
