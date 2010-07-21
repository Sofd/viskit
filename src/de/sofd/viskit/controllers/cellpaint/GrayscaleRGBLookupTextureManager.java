package de.sofd.viskit.controllers.cellpaint;

import static javax.media.opengl.GL2GL3.GL_TEXTURE_1D;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.apache.log4j.Logger;

import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.RawImage;
import de.sofd.viskit.ui.grayscale.GrayscaleUtil;

/**
 *
 * @author olaf
 */
public class GrayscaleRGBLookupTextureManager {

    protected static final Logger logger = Logger.getLogger(GrayscaleRGBLookupTextureManager.class);

    public static class TextureRef {
        private final int texId;

        public TextureRef(int texId) {
            this.texId = texId;
        }

        public int getTexId() {
            return texId;
        }
    }

    private static class TextureRefStore {
        private final LinkedHashMap<Object, TextureRef> texRefsByImageKey = new LinkedHashMap<Object, TextureRef>(256, 0.75F, true);

        public boolean containsTextureFor(Object key) {
            return texRefsByImageKey.containsKey(key);
        }

        public TextureRef getTexRef(Object key) {
            return texRefsByImageKey.get(key);
        }

        public void putTexRef(Object key, TextureRef texRef, GL gl) {
            texRefsByImageKey.put(key, texRef);
        }

    }

    private static final String TEX_STORE = "grayscaleRgbLutTexturesStore";

    public static TextureRef bindGrayscaleRGBLutTexture(GL2 gl, int texUnit, Map<String, Object> sharedContextData, ImageListViewModelElement elt) {
        TextureRefStore texRefStore = (TextureRefStore) sharedContextData.get(TEX_STORE);
        if (null == texRefStore) {
            logger.info("CREATING NEW TextureRef store for grayscale=>RGB LUT textures");
            texRefStore = new TextureRefStore();
            sharedContextData.put(TEX_STORE, texRefStore);
        }

        int bitCount = 0;
        if (elt.hasRawImage() && elt.isRawImagePreferable()) {
            RawImage rawImgProxy = elt.getProxyRawImage();
            if (rawImgProxy.getPixelFormat() == RawImage.PIXEL_FORMAT_LUMINANCE &&
                    (rawImgProxy.getPixelType() == RawImage.PIXEL_TYPE_SIGNED_16BIT || rawImgProxy.getPixelType() == RawImage.PIXEL_TYPE_UNSIGNED_16BIT)) {
                bitCount = 16;
            } else if (rawImgProxy.getPixelFormat() == RawImage.PIXEL_FORMAT_LUMINANCE &&
                       rawImgProxy.getPixelType() == RawImage.PIXEL_TYPE_UNSIGNED_12BIT) {
                bitCount = 12;
            }
        }

        int usedBitCount = Math.min(12, (bitCount == 0 ? 8 : bitCount));  // assume the GL only supports max. 2**12 1D RGB texture width => TODO: use a texture proxy to determine this at runtime
        
        TextureRef texRef = texRefStore.getTexRef(usedBitCount);
        
        if (null == texRef) {
            logger.info("need to create grayscale texture: " + usedBitCount + "-bit");
            int[] texId = new int[1];
            gl.glGenTextures(1, texId, 0);
            gl.glEnable(GL2.GL_TEXTURE_1D);
            gl.glActiveTexture(texUnit);
            gl.glBindTexture(gl.GL_TEXTURE_1D, texId[0]);
            int searchWindowWidth = (usedBitCount == 16 ? 14 : (usedBitCount == 12 ? 9 : 0));  // use this for production (maybe)
            //int searchWindowWidth = (usedBitCount == 16 ? 50 : (usedBitCount == 12 ? 40 : 0));  // use this for testing (much higher computation time, more coloured image on color screen, theoretically better grayscale reproduction on grayscale screen)
            ByteBuffer texData = GrayscaleUtil.computeGrayTo8bitRGBMappingTable(usedBitCount, searchWindowWidth);
            gl.glTexImage1D(
                    gl.GL_TEXTURE_1D,   // target
                    0,                  // level
                    gl.GL_RGB32F,      // internalFormat
                    texData.capacity() / 3,  //width
                    0,                  // border
                    gl.GL_RGB,         // format
                    gl.GL_UNSIGNED_BYTE,        // type
                    texData            // data
                    );
            gl.glTexParameteri(GL_TEXTURE_1D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
            gl.glTexParameteri(GL_TEXTURE_1D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);  // nearest neighbor filtering is important -- we don't 
            gl.glTexParameteri(GL_TEXTURE_1D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);  // want the GL to interpolate between two grayscale RGBs at any time
            texRef = new TextureRef(texId[0]);
            texRefStore.putTexRef(usedBitCount, texRef, gl);
        }
        gl.glEnable(GL2.GL_TEXTURE_1D);
        gl.glActiveTexture(texUnit);
        gl.glBindTexture(GL2.GL_TEXTURE_1D, texRef.getTexId());
        return texRef;
    }

    public static void unbindCurrentLutTexture(GL2 gl) {
        gl.glBindTexture(GL2.GL_TEXTURE_1D, 0);
    }
}
