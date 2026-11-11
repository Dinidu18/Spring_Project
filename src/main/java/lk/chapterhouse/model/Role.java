package lk.chapterhouse.model;

public enum Role {
    MEMBER, ADMIN;

    public static Role parse(String value) {
        return Role.valueOf(value.trim().toUpperCase());
    }
}
