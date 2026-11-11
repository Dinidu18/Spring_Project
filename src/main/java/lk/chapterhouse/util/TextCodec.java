package lk.chapterhouse.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** URL-safe Base64 without padding for free-text file fields (encoding, not encryption). */
public final class TextCodec {
    private TextCodec() {
    }

    public static String encode(String value) {
        String safe = value == null ? "" : value;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(safe.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return "";
        }
        try {
            return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid Base64 text field.", ex);
        }
    }
}
