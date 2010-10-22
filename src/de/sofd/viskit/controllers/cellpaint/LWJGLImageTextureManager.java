package de.sofd.viskit.controllers.cellpaint;

import java.nio.ShortBuffer;
import java.util.Map;

import org.apache.log4j.Logger;
import org.lwjgl.NondirectBufferWrapper;
import org.lwjgl.opengl.ARBTextureFloat;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.RawImage;

public class LWJGLImageTextureManager {
    protected static final Logger logger = Logger.getLogger(LWJGLImageTextureManager.class);
    
    
    public static class TextureRef {
        protected int texId;
        protected float preScale = 1.0f;
        protected float preOffset = 0.0f;

        public TextureRef() {
            
        }
        
        public TextureRef(int texId, float preScale, float preOffset) {
            this.texId = texId;
            this.preScale = preScale;
            this.preOffset = preOffset;
        }

        public int getTexId() {
            return texId;
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


    // TODO use texture store or save textures in shared context data
    public static TextureRef bindImageTexture(int texUnit, Map<String, Object> sharedContextData, ImageListViewModelElement elt/*, boolean outputGrayscaleRGBs*/) {
        
        TextureRef result = new TextureRef();
        result.texId = GL11.glGenTextures();
//        if (null == texRef) {
            logger.debug("need to create texture for: " + elt.getImageKey());
            // TODO: maybe strive to allocate textures with non-normalized, integer-valued texel values, which
            // is recommended by nVidia for 12-bit grayscale displays to preserve image fidelity (but it really
            // shouldn't make a difference in my opinion -- single precision floats normalized to [0,1] texel
            // values range should provide more than enough precision). See the
            // integer texture extension (http://www.opengl.org/registry/specs/EXT/texture_integer.txt) and
            // the nvidia document for grayscale displays (http://www.nvidia.com/docs/IO/40049/TB-04631-001_v02.pdf)
            // for details
//            Texture imageTexture = null;
            int glInternalFormat, glPixelFormat, glPixelType;
            if (elt.hasRawImage() && elt.isRawImagePreferable()) {
                RawImage rawImgProxy = elt.getProxyRawImage();
                if (rawImgProxy.getPixelFormat() == RawImage.PIXEL_FORMAT_LUMINANCE &&
                        rawImgProxy.getPixelType() == RawImage.PIXEL_TYPE_SIGNED_16BIT) {
                    logger.debug("(creating texture from raw 16-bit signed image pixel data)");
                    
                    glPixelFormat = GL11.GL_LUMINANCE;
                    glPixelType = GL11.GL_SHORT;
                    glInternalFormat = ARBTextureFloat.GL_LUMINANCE16F_ARB;
                    result.preScale = 0.7F;
                    result.preOffset = 0.1F;
                } else if (rawImgProxy.getPixelFormat() == RawImage.PIXEL_FORMAT_LUMINANCE &&
                           rawImgProxy.getPixelType() == RawImage.PIXEL_TYPE_UNSIGNED_16BIT) {
                    logger.debug("(creating texture from raw 16-bit unsigned image pixel data)");
                    
                    glPixelFormat = GL11.GL_LUMINANCE;
                    glPixelType = GL11.GL_UNSIGNED_SHORT;
                    glInternalFormat = ARBTextureFloat.GL_LUMINANCE16F_ARB; // GL_*_SNORM result in GL_INVALID_ENUM and all-white texels on tack (GeForce 8600 GT/nvidia 190.42)
                    result.preScale = 1.0F;
                    result.preOffset = 0.0F;
                } else if (rawImgProxy.getPixelFormat() == RawImage.PIXEL_FORMAT_LUMINANCE &&
                           rawImgProxy.getPixelType() == RawImage.PIXEL_TYPE_UNSIGNED_12BIT) {
                    logger.debug("(creating texture from raw 12-bit unsigned image pixel data)");
                    
                    glPixelFormat = GL11.GL_LUMINANCE;
                    glPixelType = GL11.GL_UNSIGNED_SHORT;
                    glInternalFormat = GL11.GL_LUMINANCE16; // NOT GL_LUMINANCE12 b/c pixelType is 16-bit and we'd thus lose precision
                    result.preScale = (float) (1<<16) / (1<<12);
                    result.preOffset = 0.0F;
                }
                else {
                    throw new RuntimeException("this DICOM image format is not supported for now");
                }
                RawImage rawImg = elt.getRawImage();
                
                GL13.glActiveTexture(texUnit);
                result.texId = GL11.glGenTextures();
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, result.getTexId());
                
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D,    //target
                        0,                    //level
                        glInternalFormat,     //internalFormat
                        rawImg.getWidth(), // int width,
                        rawImg.getHeight(), // int height,
                        0,                    //border
                        glPixelFormat,        //format
                        glPixelType,          //type
                        NondirectBufferWrapper.wrapDirect((ShortBuffer)rawImg.getPixelData()));    //data
                
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP );
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_WRAP_R, GL11.GL_CLAMP );
                
//            }
//            if (null == imageTexture) {
//                logger.debug("(creating texture from AWT image (fallback -- inefficient))");
//                //TextureData imageTextureData = AWTTextureIO.newTextureData(elt.getImage(), true);  // with mipmapping
//                TextureData imageTextureData = AWTTextureIO.newTextureData(elt.getImage(), false);   // w/o mipmapping
//                imageTextureData.flush();
//                GL13.glActiveTexture(texUnit);
//                imageTexture = new Texture(imageTextureData);
//            }
//            texRef = new TextureRef(imageTexture.getTextureObject(),
//                                    imageTexture.getImageTexCoords(),
//                                    imageTexture.getEstimatedMemorySize(),
//                                    preScale,
//                                    preOffset);
//            texRefStore.putTexRef(elt, texRef, gl);
//            logger.debug("GL texture memory consumption now (est.): " + (texRefStore.getTotalMemConsumption()/1024/1024) + " MB");
        }
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL13.glActiveTexture(texUnit);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, result.getTexId());
        return result;
    }

    public static void unbindCurrentImageTexture() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
}