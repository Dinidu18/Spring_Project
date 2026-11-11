package lk.chapterhouse.web.session;

public final class SessionKeys {
    public static final String USER_ID = "chapterhouse.userId";
    public static final String USER_ROLE = "chapterhouse.role";
    public static final String SESSION_VERSION = "chapterhouse.sessionVersion";
    public static final String CSRF_TOKEN = "chapterhouse.csrfToken";
    public static final String FLASH_SUCCESS = "chapterhouse.flashSuccess";
    public static final String FLASH_ERROR = "chapterhouse.flashError";
    public static final String RETURN_PATH = "chapterhouse.returnPath";

    private SessionKeys() {
    }
}
