package lk.chapterhouse.util;

import java.util.UUID;

public final class Ids {
    private Ids() {
    }

    public static String newId() {
        return UUID.randomUUID().toString();
    }

    public static boolean isUuid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            UUID.fromString(value.trim());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
