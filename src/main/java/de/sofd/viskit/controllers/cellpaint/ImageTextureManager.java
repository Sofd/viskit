package de.sofd.viskit.controllers.cellpaint;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.apache.log4j.Logger;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.awt.AWTTextureIO;

import de.sofd.viskit.image.RawImage;
import de.sofd.viskit.image.ViskitImage;
import de.sofd.viskit.model.ImageListViewModelElement;

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

        public boolean containsTextureFor(ViskitImage img) {
            return texRefsByImageKey.containsKey(img.getImageKey());
        }

        public TextureRef getTexRef(ViskitImage img) {
            return texRefsByImageKey.get(img.getImageKey());
        }

        public void putTexRef(ViskitImage img, TextureRef texRef, GL gl) {
            texRefsByImageKey.put(img.getImageKey(), texRef);
            totalMemConsumption += texRef.getMemorySize();
            freeExcessTextureMemory(gl);
        }

        public void freeExcessTextureMemory(GL gl) {
            while ((totalMemConsumption > maxMemConsumption) && (texRefsByImageKey.size() > 1)) {
                // ^^^ ensure size >= 1 to always keep at least the latest texture in, even if it alone exceeds maxMemConsumption
                //   (if that one wasn't loaded, we couldn't render it)
                Map.Entry<Object, TextureRef> oldestEntry = texRefsByImageKey.entrySet().iterator().next();
                logger.debug("deleting texture to free up memory: " + oldestEntry.getKey());
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

    private static final String TEX_STORE = "texturesStore";

    /**
     * Bind a 2D texture that contains the current image of elt into gl/texUnit.
     * 
     * @param gl
     * @param texUnit
     * @param sharedContextData
     * @param elt
     * @return
     */
    public static TextureRef bindImageTexture(GL2 gl, int texUnit, Map<String, Object> sharedContextData, ImageListViewModelElement elt/*, boolean outputGrayscaleRGBs*/) {
        ViskitImage eltImage = elt.getImage();
        TextureRefStore texRefStore = (TextureRefStore) sharedContextData.get(TEX_STORE);
        if (null == texRefStore) {
            System.out.println("CREATING NEW TextureRefStore");
            int texMemInMB;
            String memPropVal = System.getProperty(ImageTextureManager.class.getName() + ".texmem_mb");
            if (memPropVal != null) {
                try {
                    texMemInMB = Integer.parseInt(memPropVal);
                    if (texMemInMB < 2) {
                        texMemInMB = 256;
                    }
                } catch (NumberFormatException e) {
                    texMemInMB = 256;
                }
            } else {
                texMemInMB = 256;
            }
            texRefStore = new TextureRefStore(texMemInMB*1024*1024);  // <<== configure max. GL texture memory consumption here (for now)
            sharedContextData.put(TEX_STORE, texRefStore);
        }
        TextureRef texRef = texRefStore.getTexRef(eltImage);
        
        if (null == texRef) {
            logger.debug("need to create texture for: " + eltImage.getImageKey());
            // TODO: maybe strive to allocate textures with non-normalized, integer-valued texel values, which
            // is recommended by nVidia for 12-bit grayscale displays to preserve image fidelity (but it really
            // shouldn't make a difference in my opinion -- single precision floats normalized to [0,1] texel
            // values range should provide more than enough precision). See the
            // integer texture extension (http://www.opengl.org/registry/specs/EXT/texture_integer.txt) and
            // the nvidia document for grayscale displays (http://www.nvidia.com/docs/IO/40049/TB-04631-001_v02.pdf)
            // for details
            Texture imageTexture = null;
            float preScale = 1.0F, preOffset = 0.0F;
            if (eltImage.hasRawImage() && eltImage.isRawImagePreferable()) {
                RawImage rawImgProxy = eltImage.getProxyRawImage();
                if (rawImgProxy.getPixelFormat() == RawImage.PIXEL_FORMAT_LUMINANCE &&
                        rawImgProxy.getPixelType() == RawImage.PIXEL_TYPE_SIGNED_16BIT) {
                    logger.debug("(creating texture from raw 16-bit signed image pixel data)");
                    RawImage rawImg = eltImage.getRawImage();
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
                    gl.glActiveTexture(texUnit);
                    imageTexture = new Texture(imageTextureData);
                    preScale = 0.5F;
                    preOffset = 0.5F;
                } else if (rawImgProxy.getPixelFormat() == RawImage.PIXEL_FORMAT_LUMINANCE &&
                           rawImgProxy.getPixelType() == RawImage.PIXEL_TYPE_UNSIGNED_16BIT) {
                    logger.debug("(creating texture from raw 16-bit unsigned image pixel data)");
                    RawImage rawImg = eltImage.getRawImage();
                    TextureData imageTextureData =
                        new TextureData(  GL2.GL_LUMINANCE16F, // int internalFormat,  // GL_*_SNORM result in GL_INVALID_ENUM and all-white texels on tack (GeForce 8600 GT/nvidia 190.42)
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
                    gl.glActiveTexture(texUnit);
                    imageTexture = new Texture(imageTextureData);
                    preScale = 1.0F;
                    preOffset = 0.0F;
                } else if (rawImgProxy.getPixelFormat() == RawImage.PIXEL_FORMAT_LUMINANCE &&
                           rawImgProxy.getPixelType() == RawImage.PIXEL_TYPE_UNSIGNED_12BIT) {
                    logger.debug("(creating texture from raw 12-bit unsigned image pixel data)");
                    RawImage rawImg = eltImage.getRawImage();
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
                    gl.glActiveTexture(texUnit);
                    imageTexture = new Texture(imageTextureData);
                    preScale = (float) (1<<16) / (1<<12); // see doc/opengl/texture-coords-and-filtering.txt on why this is right and not (1<<16-1)/(1<<12-1)
                    preOffset = 0.0F;
                }
                
            }
            if (null == imageTexture) {
                logger.debug("(creating texture from AWT image (fallback -- inefficient))");
                //TextureData imageTextureData = AWTTextureIO.newTextureData(elt.getImage(), true);  // with mipmapping
                TextureData imageTextureData = AWTTextureIO.newTextureData(eltImage.getBufferedImage(), false);   // w/o mipmapping
                imageTextureData.flush();
                gl.glActiveTexture(texUnit);
                imageTexture = new Texture(imageTextureData);
            }
            texRef = new TextureRef(imageTexture.getTextureObject(),
                                    imageTexture.getImageTexCoords(),
                                    imageTexture.getEstimatedMemorySize(),
                                    preScale,
                                    preOffset);
            texRefStore.putTexRef(eltImage, texRef, gl);
            logger.debug("GL texture memory consumption now (est.): " + (texRefStore.getTotalMemConsumption()/1024/1024) + " MB");
        }
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glActiveTexture(texUnit);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texRef.getTexId());
        return texRef;
    }

    public static void unbindCurrentImageTexture(GL2 gl) {
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
    }
}