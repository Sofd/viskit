package de.sofd.viskit.ui.imagelist.cellviewers.jogl;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.awt.AWTTextureIO;
import de.sofd.lang.Runnable2;
import de.sofd.viskit.model.ImageListViewModelElement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.media.opengl.GL;
import org.apache.log4j.Logger;

/**
 *
 * @author olaf
 */
class ImageTextureManager {

    protected static final Logger logger = Logger.getLogger(ImageTextureManager.class);

    public static class TextureRef {
        private final int texId;
        private final TextureCoords coords;
        private final int memorySize;

        public TextureRef(int texId, TextureCoords coords, int memorySize) {
            this.texId = texId;
            this.coords = coords;
            this.memorySize = memorySize;
        }

        public int getTexId() {
            return texId;
        }

        public TextureCoords getCoords() {
            return coords;
        }

        public int getMemorySize() {
            return memorySize;
        }

    }

    private static class TextureRefStore {
        private Map<Object, TextureRef> texRefsByImageKey = new LinkedHashMap<Object, TextureRef>() {
            @Override
            protected boolean removeEldestEntry(Entry<Object, TextureRef> eldest) {
                return size() > 50;   // TODO: account for texture memory consumption here
                   // TODO: glDeleteTexture textures as their IDs are evicted from the cache
            }
        };

        public boolean containsTextureFor(ImageListViewModelElement elt) {
            return texRefsByImageKey.containsKey(elt.getImageKey());
        }

        public TextureRef getTexRef(ImageListViewModelElement elt) {
            return texRefsByImageKey.get(elt.getImageKey());
        }

        public TextureRef getTexRef(Object imageKey) {
            return texRefsByImageKey.get(imageKey);
        }

        public void putTexRef(ImageListViewModelElement elt, TextureRef texRef) {
            texRefsByImageKey.put(elt.getImageKey(), texRef);
        }
    }

    // TODO: ugly staticnesses in here...

    private static boolean initialized = false;
    private static final String TEX_STORE = "texturesStore";

    public static void init() {
        if (initialized) { return; }
        SharedContextData.registerContextInitCallback(new Runnable2<SharedContextData, GL>() {
            @Override
            public void run(SharedContextData cd, GL gl1) {
                TextureRefStore texturesStore = new TextureRefStore();
                cd.setAttribute(TEX_STORE, texturesStore);
            }
        });
    }

    static {
        init();
    }

    public static TextureRef bindImageTexture(SharedContextData cd, ImageListViewModelElement elt) {
        TextureRefStore texRefStore = (TextureRefStore) cd.getAttribute(TEX_STORE);
        TextureRef texRef = texRefStore.getTexRef(elt);
        if (null == texRef) {
            logger.info("need to re-create texture for image " + elt.getImageKey());
            TextureData imageTextureData = AWTTextureIO.newTextureData(elt.getImage(), true);
            imageTextureData.flush();
            Texture imageTexture = new Texture(imageTextureData);
            texRef = new TextureRef(imageTexture.getTextureObject(), imageTexture.getImageTexCoords(), imageTexture.getEstimatedMemorySize());
            texRefStore.putTexRef(elt, texRef);
        }
        cd.getGlContext().getCurrentGL().glEnable(GL.GL_TEXTURE_2D);
        cd.getGlContext().getCurrentGL().glBindTexture(GL.GL_TEXTURE_2D, texRef.getTexId());
        return texRef;
    }

    public static void unbindCurrentImageTexture(SharedContextData cd) {
        cd.getGlContext().getCurrentGL().glBindTexture(GL.GL_TEXTURE_2D, 0);
    }
}
