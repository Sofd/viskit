package de.sofd.viskit.controllers.cellpaint.texturemanager;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.sofd.viskit.model.LookupTable;

public abstract class LookupTableTextureManager {
    
    
    protected static final Logger logger = Logger.getLogger(LookupTableTextureManager.class);
    
    protected static final String TEX_STORE = "lutTexturesStore";
    
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
        private final LinkedHashMap<LookupTable, TextureRef> texRefsByImageKey = new LinkedHashMap<LookupTable, TextureRef>(256, 0.75F, true);

        public boolean containsTextureFor(LookupTable lut) {
            return texRefsByImageKey.containsKey(lut);
        }

        public TextureRef getTexRef(LookupTable lut) {
            return texRefsByImageKey.get(lut);
        }

        public void putTexRef(LookupTable lut, TextureRef texRef) {
            texRefsByImageKey.put(lut, texRef);
        }

    }
    
    public abstract void unbindCurrentLutTexture(Object glContext);
    
    public abstract TextureRef bindLutTexture(Object glContext, int texUnit, Map<String, Object> sharedContextData, LookupTable lut);

    

}
