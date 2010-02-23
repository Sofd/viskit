package de.sofd.viskit.ui.imagelist.cellviewers.jogl;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.awt.AWTTextureIO;
import de.sofd.lang.Runnable2;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.RawImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import org.apache.log4j.Logger;

/**
 *
 * @author olaf
 */
public class ImageTextureManager {

    protected static final Logger logger = Logger.getLogger(ImageTextureManager.class);

    public static class TextureRef {
        private final int texId;
        private final TextureCoords coords;
        private final int memorySize;
        private final float preScale;
        private final float preOffset;

        public TextureRef(int texId, TextureCoords coords, int memorySize, float preScale, float preOffset) {
            this.texId = texId;
            this.coords = coords;
            this.memorySize = memorySize;
            this.preScale = preScale;
            this.preOffset = preOffset;
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

        /**
         * preScale/preOffset: linear transformation to be applied to texel
         * values by the shader to normalize to [0..1] range. preScale * (texel
         * value) + preOffset must transform all texel values to that range
         */
        public float getPreScale() {
            return preScale;
        }
        
        public float getPreOffset() {
            return preOffset;
        }
    }

    private static class TextureRefStore {
        private long totalMemConsumption = 0;
        private final long maxMemConsumption;

        private final LinkedHashMap<Object, TextureRef> texRefsByImageKey = new LinkedHashMap<Object, TextureRef>(256, 0.75F, true);

        public TextureRefStore(long maxMemConsumption) {
            this.maxMemConsumption = maxMemConsumption;
        }

        public boolean containsTextureFor(ImageListViewModelElement elt) {
            return texRefsByImageKey.containsKey(elt.getImageKey());
        }

        public TextureRef getTexRef(ImageListViewModelElement elt) {
            return texRefsByImageKey.get(elt.getImageKey());
        }

        public TextureRef getTexRef(Object imageKey) {
            return texRefsByImageKey.get(imageKey);
        }

        public void putTexRef(ImageListViewModelElement elt, TextureRef texRef, GL gl) {
            texRefsByImageKey.put(elt.getImageKey(), texRef);
            totalMemConsumption += texRef.getMemorySize();
            freeExcessTextureMemory(gl);
        }

        public void freeExcessTextureMemory(GL gl) {
            while ((totalMemConsumption > maxMemConsumption) && (texRefsByImageKey.size() > 1)) {
                // ^^^ ensure size >= 1 to always keep at least the latest texture in, even if it alone exceeds maxMemConsumption
                //   (if that one wasn't loaded, we couldn't render it)
                Map.Entry<Object, TextureRef> oldestEntry = texRefsByImageKey.entrySet().iterator().next();
                logger.info("deleting texture to free up memory: " + oldestEntry.getKey());
                TextureRef oldestTexRef = oldestEntry.getValue();
                gl.glDeleteTextures(1, new int[]{oldestTexRef.getTexId()}, 0);
                totalMemConsumption -= oldestTexRef.getMemorySize();
                texRefsByImageKey.remove(oldestEntry.getKey());
            }
        }

        public long getTotalMemConsumption() {
            return totalMemConsumption;
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
                TextureRefStore texturesStore = new TextureRefStore(256*1024*1024);  // <<== configure max. GL texture memory consumption here (for now)
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
            logger.info("need to create texture for: " + elt.getImageKey());
            Texture imageTexture = null;
            float preScale = 1.0F, preOffset = 0.0F;
            if (elt.hasRawImage() && elt.isRawImagePreferable()) {
                RawImage rawImgProxy = elt.getProxyRawImage();
                if (rawImgProxy.getPixelFormat() == RawImage.PIXEL_FORMAT_LUMINANCE &&
                        rawImgProxy.getPixelType() == RawImage.PIXEL_TYPE_SIGNED_16BIT) {
                    logger.info("(creating texture from raw 16-bit signed image pixel data)");
                    RawImage rawImg = elt.getRawImage();
                    TextureData imageTextureData =
                        new TextureData(  GL2.GL_LUMINANCE16F, // int internalFormat,  // GL_*_SNORM result in GL_INVALID_ENUM and all-white texels on tack (GeForce 8600 GT/nvidia 190.42)
                                          rawImg.getWidth(), // int width,
                                          rawImg.getHeight(), // int height,
                                          0,     // int border,
                                          GL.GL_LUMINANCE, // int pixelFormat,
                                          GL.GL_SHORT, // int pixelType,
                                          false, // boolean mipmap,
                                          false, // boolean dataIsCompressed,
                                          false, // boolean mustFlipVertically,  // TODO: correct?
                                          rawImg.getPixelData(), // Buffer buffer,
                                          null // Flusher flusher);
                                          );
                    imageTextureData.flush();
                    cd.getGlContext().getCurrentGL().glActiveTexture(GL2.GL_TEXTURE1);
                    imageTexture = new Texture(imageTextureData);
                    preScale = 0.5F;
                    preOffset = 0.5F;
                } else if (rawImgProxy.getPixelFormat() == RawImage.PIXEL_FORMAT_LUMINANCE &&
                           rawImgProxy.getPixelType() == RawImage.PIXEL_TYPE_UNSIGNED_12BIT) {
                    logger.info("(creating texture from raw 12-bit unsigned image pixel data)");
                    RawImage rawImg = elt.getRawImage();
                    TextureData imageTextureData =
                        new TextureData(  GL2.GL_LUMINANCE16, // NOT GL_LUMINANCE12 b/c pixelType is 16-bit and we'd thus lose precision
                                          rawImg.getWidth(), // int width,
                                          rawImg.getHeight(), // int height,
                                          0,     // int border,
                                          GL.GL_LUMINANCE, // int pixelFormat,
                                          GL.GL_UNSIGNED_SHORT, // int pixelType,
                                          false, // boolean mipmap,
                                          false, // boolean dataIsCompressed,
                                          false, // boolean mustFlipVertically,  // TODO: correct?
                                          rawImg.getPixelData(), // Buffer buffer,
                                          null // Flusher flusher);
                                          );
                    imageTextureData.flush();
                    cd.getGlContext().getCurrentGL().glActiveTexture(GL2.GL_TEXTURE1);
                    imageTexture = new Texture(imageTextureData);
                    preScale = (float) (1<<16) / (1<<12);
                    preOffset = 0.0F;
                }
                
            }
            if (null == imageTexture) {
                //TextureData imageTextureData = AWTTextureIO.newTextureData(elt.getImage(), true);  // with mipmapping
                TextureData imageTextureData = AWTTextureIO.newTextureData(elt.getImage(), false);   // w/o mipmapping
                imageTextureData.flush();
                cd.getGlContext().getCurrentGL().glActiveTexture(GL2.GL_TEXTURE1);
                imageTexture = new Texture(imageTextureData);
            }
            texRef = new TextureRef(imageTexture.getTextureObject(),
                                    imageTexture.getImageTexCoords(),
                                    imageTexture.getEstimatedMemorySize(),
                                    preScale,
                                    preOffset);
            texRefStore.putTexRef(elt, texRef, cd.getGlContext().getCurrentGL());
            logger.info("GL texture memory consumption now (est.): " + (texRefStore.getTotalMemConsumption()/1024/1024) + " MB");
        }
        cd.getGlContext().getCurrentGL().glEnable(GL.GL_TEXTURE_2D);
        cd.getGlContext().getCurrentGL().glActiveTexture(GL2.GL_TEXTURE1);
        cd.getGlContext().getCurrentGL().glBindTexture(GL.GL_TEXTURE_2D, texRef.getTexId());
        return texRef;
    }

    public static void unbindCurrentImageTexture(SharedContextData cd) {
        cd.getGlContext().getCurrentGL().glBindTexture(GL.GL_TEXTURE_2D, 0);
    }
}
