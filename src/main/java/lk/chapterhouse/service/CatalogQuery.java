package lk.chapterhouse.service;

import java.util.Locale;

/** Filters and pagination for public catalog browsing. */
public record CatalogQuery(
        String searchText,
        String categoryId,
        String bookType,
        Boolean featuredOnly,
        SortField sortField,
        boolean sortDescending,
        int page,
        int pageSize
) {
    public enum SortField {
        TITLE, AUTHOR, PRICE, YEAR, CREATED;

        public static SortField parse(String value) {
            if (value == null || value.isBlank()) {
                return TITLE;
            }
            return SortField.valueOf(value.trim().toUpperCase(Locale.ROOT));
        }
    }

    public CatalogQuery {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 12;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }
        if (sortField == null) {
            sortField = SortField.TITLE;
        }
    }

    public static CatalogQuery defaults() {
        return new CatalogQuery(null, null, null, null, SortField.TITLE, false, 1, 12);
    }

    /** JavaBean aliases so JSP/EL can read record components. */
    public String getSearchText() {
        return searchText;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getBookType() {
        return bookType;
    }

    public Boolean getFeaturedOnly() {
        return featuredOnly;
    }

    public SortField getSortField() {
        return sortField;
    }

    public boolean isSortDescending() {
        return sortDescending;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }
}
