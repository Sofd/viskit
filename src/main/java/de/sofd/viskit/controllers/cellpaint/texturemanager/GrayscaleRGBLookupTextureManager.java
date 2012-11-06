package de.sofd.viskit.controllers.cellpaint.texturemanager;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.sofd.viskit.model.ImageListViewModelElement;

public abstract class GrayscaleRGBLookupTextureManager {

    protected static final Logger logger = Logger.getLogger(GrayscaleRGBLookupTextureManager.class);
    
    protected static final String TEX_STORE = "grayscaleRgbLutTexturesStore";

    public static class TextureRef {
        private final int texId;

        public TextureRef(int texId) {
            this.texId = texId;
        }

        public int getTexId() {
            return texId;
        }
    }

    protected static class TextureRefStore {
        private final LinkedHashMap<Object, TextureRef> texRefsByImageKey = new LinkedHashMap<Object, TextureRef>(256, 0.75F, true);

        public boolean containsTextureFor(Object key) {
            return texRefsByImageKey.containsKey(key);
        }

        public TextureRef getTexRef(Object key) {
            return texRefsByImageKey.get(key);
        }

        public void putTexRef(Object key, TextureRef texRef) {
            texRefsByImageKey.put(key, texRef);
        }

    }
    public abstract TextureRef bindGrayscaleRGBLutTexture(Object glContext, int texUnit, Map<String, Object> sharedContextData, ImageListViewModelElement elt);
    
    public abstract void unbindCurrentLutTexture(Object glContext);

    
}
