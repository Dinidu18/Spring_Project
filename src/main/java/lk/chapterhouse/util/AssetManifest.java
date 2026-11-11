package lk.chapterhouse.util;

import java.util.LinkedHashMap;
import java.util.Map;

/** Whitelist of catalog image keys to web paths and alt text. */
public final class AssetManifest {
    private static final Map<String, AssetEntry> ENTRIES = new LinkedHashMap<>();

    static {
        cover("covers/lantern-archive.jpg", "/assets/img/covers/lantern-archive.jpg", "The Lantern Archive cover");
        cover("covers/monsoon-letters.jpg", "/assets/img/covers/monsoon-letters.jpg", "Monsoon Letters cover");
        cover("covers/cloud-patterns.jpg", "/assets/img/covers/cloud-patterns.jpg", "Cloud Patterns cover");
        cover("covers/effective-java.jpg", "/assets/img/covers/effective-java.jpg", "Effective Java cover");
        cover("covers/design-patterns-explained.jpg", "/assets/img/covers/design-patterns.jpg", "Design Patterns Explained cover");
        cover("covers/clean-code.jpg", "/assets/img/covers/clean-code.jpg", "Clean Code cover");
        cover("covers/sre.jpg", "/assets/img/covers/sre.jpg", "Site Reliability Engineering cover");
        cover("covers/lean-startup.jpg", "/assets/img/covers/lean-startup.jpg", "The Lean Startup cover");
        cover("covers/deep-work.jpg", "/assets/img/covers/deep-work.jpg", "Deep Work cover");
        cover("covers/atomic-habits.jpg", "/assets/img/covers/atomic-habits.jpg", "Atomic Habits cover");
        cover("covers/kite-runner.jpg", "/assets/img/covers/kite-runner.jpg", "The Kite Runner cover");
        cover("covers/sapiens.jpg", "/assets/img/covers/sapiens.jpg", "Sapiens cover");
        cover("covers/book-thief.jpg", "/assets/img/covers/book-thief.jpg", "The Book Thief cover");
        cover("covers/goodnight-room.jpg", "/assets/img/covers/goodnight-room.jpg", "Goodnight Moon Room cover");
        cover("covers/mindful-mornings.jpg", "/assets/img/covers/mindful-mornings.jpg", "Mindful Mornings cover");
        cover("covers/colonial-routes.jpg", "/assets/img/covers/colonial-routes.jpg", "Colonial Routes cover");
        cover("covers/startup-finance.jpg", "/assets/img/covers/startup-finance.jpg", "Startup Finance Basics cover");
        cover("covers/pragmatic-programmer.jpg", "/assets/img/covers/pragmatic-programmer.jpg", "The Pragmatic Programmer cover");
        cover("covers/harbor-secrets.jpg", "/assets/img/covers/harbor-secrets.jpg", "Harbor of Secrets cover");
        cover("covers/tea-trails.jpg", "/assets/img/covers/tea-trails.jpg", "Tea Trails cover");
        cover("covers/salt-cinnamon.jpg", "/assets/img/covers/salt-cinnamon.jpg", "Salt and Cinnamon cover");
        cover("covers/night-market.jpg", "/assets/img/covers/harbor-secrets.jpg", "Night Market Notes cover");
        cover("covers/quiet-algorithms.jpg", "/assets/img/covers/effective-java.jpg", "Quiet Algorithms cover");
        cover("covers/garden-of-maps.jpg", "/assets/img/covers/tea-trails.jpg", "Garden of Maps cover");

        category("categories/fiction.jpg", "/assets/img/categories/fiction.jpg", "Fiction category");
        category("categories/science-tech.jpg", "/assets/img/categories/science-tech.jpg", "Science and Technology category");
        category("categories/business.jpg", "/assets/img/categories/business.jpg", "Business category");
        category("categories/children.jpg", "/assets/img/categories/children.jpg", "Children category");
        category("categories/history.jpg", "/assets/img/categories/history.jpg", "History category");
        category("categories/self-help.jpg", "/assets/img/categories/self-help.jpg", "Self Help category");
        category("categories/mystery.jpg", "/assets/img/categories/mystery.jpg", "Mystery category");
        category("categories/travel.jpg", "/assets/img/categories/travel.jpg", "Travel category");
        category("categories/poetry.jpg", "/assets/img/categories/poetry.jpg", "Poetry category");
    }

    private AssetManifest() {
    }

    private static void cover(String key, String path, String alt) {
        ENTRIES.put(key, new AssetEntry(path, alt));
    }

    private static void category(String key, String path, String alt) {
        ENTRIES.put(key, new AssetEntry(path, alt));
    }

    public static String assetUrl(String key) {
        if (key == null || key.isBlank()) {
            return "/assets/img/covers/default.jpg";
        }
        AssetEntry entry = ENTRIES.get(key.trim());
        if (entry != null) {
            return entry.path();
        }
        return "/assets/img/" + key;
    }

    public static String assetAlt(String key) {
        if (key == null || key.isBlank()) {
            return "Book cover";
        }
        AssetEntry entry = ENTRIES.get(key.trim());
        return entry != null ? entry.alt() : "Catalog image";
    }

    public static Map<String, AssetEntry> allEntries() {
        return Map.copyOf(ENTRIES);
    }

    public record AssetEntry(String path, String alt) {
    }
}
