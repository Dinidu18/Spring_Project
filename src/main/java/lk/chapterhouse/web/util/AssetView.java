package lk.chapterhouse.web.util;

import lk.chapterhouse.util.AssetManifest;
import org.springframework.stereotype.Component;

@Component
public class AssetView {
    public String assetUrl(String key) {
        return AssetManifest.assetUrl(key);
    }

    public String assetAlt(String key) {
        return AssetManifest.assetAlt(key);
    }
}
