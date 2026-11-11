package lk.chapterhouse.model;

import java.math.BigDecimal;
import java.time.Instant;

public class EBook extends Book {
    public static final String TYPE = "EBOOK";
    public static final String PDF = "PDF";
    public static final String EPUB = "EPUB";

    private String fileFormat;
    private BigDecimal fileSizeMb;
    private boolean digitalAvailable;

    public EBook(String id, String title, String author, String description, String isbn,
                 int publicationYear, String categoryId, BigDecimal price, String coverImageKey,
                 boolean featured, Instant createdAt, Instant updatedAt, long recordVersion,
                 String fileFormat, BigDecimal fileSizeMb, boolean digitalAvailable) {
        super(id, title, author, description, isbn, publicationYear, categoryId, price, coverImageKey,
                featured, createdAt, updatedAt, recordVersion);
        setFileFormat(fileFormat);
        setFileSizeMb(fileSizeMb);
        this.digitalAvailable = digitalAvailable;
    }

    public String getFileFormat() { return fileFormat; }
    public BigDecimal getFileSizeMb() { return fileSizeMb; }
    public boolean isDigitalAvailable() { return digitalAvailable; }

    public void setFileFormat(String fileFormat) {
        String value = fileFormat == null ? "" : fileFormat.trim().toUpperCase();
        if (!PDF.equals(value) && !EPUB.equals(value)) {
            throw new IllegalArgumentException("File format must be PDF or EPUB.");
        }
        this.fileFormat = value;
    }

    public void setFileSizeMb(BigDecimal fileSizeMb) {
        if (fileSizeMb == null || fileSizeMb.compareTo(BigDecimal.ZERO) <= 0
                || fileSizeMb.compareTo(new BigDecimal("10000")) > 0) {
            throw new IllegalArgumentException("File size must be greater than 0 and at most 10000 MB.");
        }
        this.fileSizeMb = fileSizeMb.stripTrailingZeros();
        if (this.fileSizeMb.scale() < 0) {
            this.fileSizeMb = this.fileSizeMb.setScale(0);
        }
    }

    public void setDigitalAvailable(boolean digitalAvailable) {
        this.digitalAvailable = digitalAvailable;
    }

    @Override
    public String getTypeCode() {
        return TYPE;
    }

    @Override
    public String getFormatDetails() {
        return fileFormat + ", " + fileSizeMb.toPlainString() + " MB"
                + (digitalAvailable ? ", available digitally" : ", currently unavailable");
    }

    @Override
    public boolean isAvailable() {
        return digitalAvailable;
    }
}
