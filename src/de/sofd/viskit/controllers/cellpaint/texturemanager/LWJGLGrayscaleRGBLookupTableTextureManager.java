package de.sofd.viskit.controllers.cellpaint.texturemanager;

import java.nio.ByteBuffer;
import java.util.Map;

import org.lwjgl.NondirectBufferWrapper;
import org.lwjgl.opengl.ARBTextureFloat;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.RawImage;
import de.sofd.viskit.ui.grayscale.GrayscaleUtil;

public class LWJGLGrayscaleRGBLookupTableTextureManager extends GrayscaleRGBLookupTextureManager {
    
    
    private static final GrayscaleRGBLookupTextureManager manager = new LWJGLGrayscaleRGBLookupTableTextureManager();
    
    private LWJGLGrayscaleRGBLookupTableTextureManager() {
        
    }
    
    public static GrayscaleRGBLookupTextureManager getInstance() {
        return manager;
    }
    
    @Override
    public TextureRef bindGrayscaleRGBLutTexture(Object glContext, int texUnit, Map<String, Object> sharedContextData,
            ImageListViewModelElement elt) {
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
            int texId = GL11.glGenTextures();
            GL11.glEnable(GL11.GL_TEXTURE_1D);
            GL13.glActiveTexture(texUnit);
            GL11.glBindTexture(GL11.GL_TEXTURE_1D, texId);
            int searchWindowWidth = (usedBitCount == 16 ? 14 : (usedBitCount == 12 ? 9 : 0));  // use this for production (maybe)
            //int searchWindowWidth = (usedBitCount == 16 ? 50 : (usedBitCount == 12 ? 40 : 0));  // use this for testing (much higher computation time, more coloured image on color screen, theoretically better grayscale reproduction on grayscale screen)
            ByteBuffer texData = GrayscaleUtil.computeGrayTo8bitRGBMappingTable(usedBitCount, searchWindowWidth);
            GL11.glTexImage1D(
                    GL11.GL_TEXTURE_1D,   // target
                    0,                  // level
                    ARBTextureFloat.GL_RGB32F_ARB,      // internalFormat
                    texData.capacity() / 3,  //width
                    0,                  // border
                    GL11.GL_RGB,         // format
                    GL11.GL_UNSIGNED_BYTE,        // type
                    NondirectBufferWrapper.wrapDirect(texData)            // data
                    );
            GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);  // nearest neighbor filtering is important -- we don't 
            GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);  // want the GL to interpolate between two grayscale RGBs at any time
            texRef = new TextureRef(texId);
            texRefStore.putTexRef(usedBitCount, texRef);
        }
        GL11.glEnable(GL11.GL_TEXTURE_1D);
        GL13.glActiveTexture(texUnit);
        GL11.glBindTexture(GL11.GL_TEXTURE_1D, texRef.getTexId());
        return texRef;
    }

    @Override
    public void unbindCurrentLutTexture(Object glContext) {
        GL11.glBindTexture(GL11.GL_TEXTURE_1D, 0);
    }
}
