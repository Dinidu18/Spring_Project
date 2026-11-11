package lk.chapterhouse.service;

import java.util.List;

import lk.chapterhouse.model.Book;

public record CatalogPage(List<Book> items, int page, int pageSize, int totalItems, int totalPages) {
    public CatalogPage {
        items = List.copyOf(items);
    }

    /** JavaBean aliases so JSP/EL can read record components. */
    public List<Book> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
