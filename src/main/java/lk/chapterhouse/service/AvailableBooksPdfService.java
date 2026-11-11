package lk.chapterhouse.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lk.chapterhouse.model.Book;
import lk.chapterhouse.model.Category;
import lk.chapterhouse.util.AppClock;

/** Builds a printable PDF catalog of books available right now. */
public class AvailableBooksPdfService {
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm").withLocale(Locale.UK);

    private final BookService books;
    private final CategoryService categories;

    public AvailableBooksPdfService(BookService books, CategoryService categories) {
        this.books = books;
        this.categories = categories;
    }

    public byte[] buildPdf() throws IOException {
        List<Book> available = books.listAvailable();
        Map<String, String> categoryNames = categoryNameMap();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 40, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(17, 70, 54));
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(64, 82, 72));
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(19, 32, 25));

            Paragraph title = new Paragraph("Chapter House - Available Books", titleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            document.add(title);

            String generated = DATE_FMT.format(AppClock.now().atZone(ZoneId.systemDefault()));
            Paragraph meta = new Paragraph(
                    "In-store availability as of " + generated
                            + "  |  " + available.size() + " title(s) currently available",
                    subFont);
            meta.setSpacingBefore(4);
            meta.setSpacingAfter(14);
            document.add(meta);

            if (available.isEmpty()) {
                document.add(new Paragraph("No books are available right now.", cellFont));
            } else {
                PdfPTable table = new PdfPTable(new float[]{3.2f, 2.2f, 1.1f, 1.8f, 1.1f, 2.6f});
                table.setWidthPercentage(100);
                addHeader(table, headFont, "Title", "Author", "Format", "Category", "Price", "Availability");
                Color zebra = new Color(238, 243, 240);
                int row = 0;
                for (Book book : available) {
                    Color bg = row % 2 == 0 ? Color.WHITE : zebra;
                    addCell(table, cellFont, book.getTitle(), bg, Element.ALIGN_LEFT);
                    addCell(table, cellFont, book.getAuthor(), bg, Element.ALIGN_LEFT);
                    addCell(table, cellFont, book.getTypeCode(), bg, Element.ALIGN_CENTER);
                    addCell(table, cellFont,
                            categoryNames.getOrDefault(book.getCategoryId(), "—"),
                            bg, Element.ALIGN_LEFT);
                    addCell(table, cellFont, "Rs. " + book.getPrice().toPlainString(), bg, Element.ALIGN_RIGHT);
                    addCell(table, cellFont, book.getFormatDetails(), bg, Element.ALIGN_LEFT);
                    row++;
                }
                document.add(table);
            }

            Paragraph footer = new Paragraph(
                    "Chapter House Online Bookstore",
                    FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(107, 122, 113)));
            footer.setSpacingBefore(16);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (DocumentException ex) {
            throw new IOException("Failed to create available-books PDF.", ex);
        }
    }

    private Map<String, String> categoryNameMap() {
        Map<String, String> map = new HashMap<>();
        for (Category category : categories.listAll()) {
            map.put(category.getId(), category.getName());
        }
        return map;
    }

    private static void addHeader(PdfPTable table, Font font, String... labels) {
        Color headerBg = new Color(28, 107, 82);
        for (String label : labels) {
            PdfPCell cell = new PdfPCell(new Phrase(label, font));
            cell.setBackgroundColor(headerBg);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);
        }
    }

    private static void addCell(PdfPTable table, Font font, String text, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }
}
