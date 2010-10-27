package de.sofd.viskit.controllers.cellpaint.texturemanager;

import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.awt.AWTTextureIO;

import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.RawImage;

/**
 * 
 * @author honglinh
 *
 */
public class JGLImageTextureManager extends ImageTextureManager {
    
    private static final ImageTextureManager texManager = new JGLImageTextureManager();

    private JGLImageTextureManager() {
    }
    
    public static ImageTextureManager getInstance() {
        return texManager;
    }
    
    private boolean flipVertically = false;
    
    @Override
    public TextureRef bindImageTexture(Object glContext, int texUnit, Map<String, Object> sharedContextData, ImageListViewModelElement elt) {
        GL2 gl = getGL2(glContext);
        
        TextureRefStore texRefStore = (TextureRefStore) sharedContextData.get(TEX_STORE);
        if (null == texRefStore) {
            texRefStore = createRefStore(sharedContextData);
        }
        TextureRef texRef = texRefStore.getTexRef(elt);
        
        if (null == texRef) {
            logger.debug("need to create texture for: " + elt.getImageKey());
            // TODO: maybe strive to allocate textures with non-normalized, integer-valued texel values, which
            // is recommended by nVidia for 12-bit grayscale displays to preserve image fidelity (but it really
            // shouldn't make a difference in my opinion -- single precision floats normalized to [0,1] texel
            // values range should provide more than enough precision). See the
            // integer texture extension (http://www.opengl.org/registry/specs/EXT/texture_integer.txt) and
            // the nvidia document for grayscale displays (http://www.nvidia.com/docs/IO/40049/TB-04631-001_v02.pdf)
            // for details
            Texture imageTexture = null;
            float preScale = 1.0F, preOffset = 0.0F;
            if (elt.hasRawImage() && elt.isRawImagePreferable()) {
                RawImage rawImgProxy = elt.getProxyRawImage();
                if (rawImgProxy.getPixelFormat() == RawImage.PIXEL_FORMAT_LUMINANCE &&
                        rawImgProxy.getPixelType() == RawImage.PIXEL_TYPE_SIGNED_16BIT) {
                    logger.debug("(creating texture from raw 16-bit signed image pixel data)");
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
                                          flipVertically, // boolean mustFlipVertically,  // TODO: correct?
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
                    RawImage rawImg = elt.getRawImage();
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
                    gl.glActiveTexture(texUnit);
                    imageTexture = new Texture(imageTextureData);
                    preScale = (float) (1<<16) / (1<<12); // see doc/opengl/texture-coords-and-filtering.txt on why this is right and not (1<<16-1)/(1<<12-1)
                    preOffset = 0.0F;
                }
                
            }
            if (null == imageTexture) {
                logger.debug("(creating texture from AWT image (fallback -- inefficient))");
                //TextureData imageTextureData = AWTTextureIO.newTextureData(elt.getImage(), true);  // with mipmapping
                TextureData imageTextureData = AWTTextureIO.newTextureData(elt.getImage(), false);   // w/o mipmapping
                imageTextureData.flush();
                gl.glActiveTexture(texUnit);
                imageTexture = new Texture(imageTextureData);
            }
            texRef = new TextureRef(imageTexture.getTextureObject(),
                                    imageTexture.getEstimatedMemorySize(),
                                    preScale,
                                    preOffset,
                                    flipVertically); // flip vertically 
            texRefStore.putTexRef(elt, texRef);
            logger.debug("GL texture memory consumption now (est.): " + (texRefStore.getTotalMemConsumption()/1024/1024) + " MB");
        }
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glActiveTexture(texUnit);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texRef.getTexId());
        return texRef;
    }

    @Override
    protected void glDeleteTextures(Object glContext, int n, int[] textures, int textures_offset) {
        GL2 gl = getGL2(glContext);
        gl.glDeleteTextures(1, textures, textures_offset);
    }

    @Override
    public void unbindCurrentImageTexture(Object glContext) {
        GL2 gl = getGL2(glContext);
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
    }
    
    private GL2 getGL2(Object glContext) {
        GL2 gl = null;
        if(glContext instanceof GL2) {
            gl = (GL2) glContext;
        }
        else throw new IllegalStateException("No GL2 Context passed!");
        return gl;
    }
}
