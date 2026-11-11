package lk.chapterhouse.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

public class Category {
    private final String id;
    private String name;
    private String normalizedName;
    private String description;
    private String imageKey;
    private final Instant createdAt;
    private Instant updatedAt;
    private long recordVersion;

    public Category(String id, String name, String description, String imageKey,
                    Instant createdAt, Instant updatedAt, long recordVersion) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Category id is required.");
        }
        this.id = id.trim();
        setName(name);
        setDescription(description);
        setImageKey(imageKey);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.recordVersion = recordVersion;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getNormalizedName() { return normalizedName; }
    public String getDescription() { return description; }
    public String getImageKey() { return imageKey; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getRecordVersion() { return recordVersion; }

    public void setName(String name) {
        if (name == null || name.trim().length() < 2 || name.trim().length() > 60) {
            throw new IllegalArgumentException("Category name must be 2 to 60 characters.");
        }
        this.name = name.trim();
        this.normalizedName = this.name.toLowerCase(Locale.ROOT);
    }

    public void setDescription(String description) {
        if (description == null || description.trim().length() < 10 || description.trim().length() > 1000) {
            throw new IllegalArgumentException("Category description must be 10 to 1000 characters.");
        }
        this.description = description.trim();
    }

    public void setImageKey(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            throw new IllegalArgumentException("Category image key is required.");
        }
        this.imageKey = imageKey.trim();
    }

    public void touch(Instant when, long newVersion) {
        this.updatedAt = when;
        this.recordVersion = newVersion;
    }
}
