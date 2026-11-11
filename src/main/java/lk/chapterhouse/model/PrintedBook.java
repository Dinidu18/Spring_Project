package lk.chapterhouse.model;

import java.math.BigDecimal;
import java.time.Instant;

public class PrintedBook extends Book {
    public static final String TYPE = "PRINTED";
    public static final String PAPERBACK = "PAPERBACK";
    public static final String HARDCOVER = "HARDCOVER";

    private int stockQuantity;
    private int pageCount;
    private String binding;

    public PrintedBook(String id, String title, String author, String description, String isbn,
                       int publicationYear, String categoryId, BigDecimal price, String coverImageKey,
                       boolean featured, Instant createdAt, Instant updatedAt, long recordVersion,
                       int stockQuantity, int pageCount, String binding) {
        super(id, title, author, description, isbn, publicationYear, categoryId, price, coverImageKey,
                featured, createdAt, updatedAt, recordVersion);
        setStockQuantity(stockQuantity);
        setPageCount(pageCount);
        setBinding(binding);
    }

    public int getStockQuantity() { return stockQuantity; }
    public int getPageCount() { return pageCount; }
    public String getBinding() { return binding; }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0 || stockQuantity > 100000) {
            throw new IllegalArgumentException("Stock quantity must be 0 to 100000.");
        }
        this.stockQuantity = stockQuantity;
    }

    public void setPageCount(int pageCount) {
        if (pageCount <= 0 || pageCount > 20000) {
            throw new IllegalArgumentException("Page count must be greater than zero.");
        }
        this.pageCount = pageCount;
    }

    public void setBinding(String binding) {
        String value = binding == null ? "" : binding.trim().toUpperCase();
        if (!PAPERBACK.equals(value) && !HARDCOVER.equals(value)) {
            throw new IllegalArgumentException("Binding must be PAPERBACK or HARDCOVER.");
        }
        this.binding = value;
    }

    @Override
    public String getTypeCode() {
        return TYPE;
    }

    @Override
    public String getFormatDetails() {
        String label = PAPERBACK.equals(binding) ? "Paperback" : "Hardcover";
        return label + ", " + pageCount + " pages, " + stockQuantity + " in stock";
    }

    @Override
    public boolean isAvailable() {
        return stockQuantity > 0;
    }
}
