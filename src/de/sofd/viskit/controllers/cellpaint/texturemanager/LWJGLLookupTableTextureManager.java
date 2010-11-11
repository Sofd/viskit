package de.sofd.viskit.controllers.cellpaint.texturemanager;

import java.nio.FloatBuffer;
import java.util.Map;

import org.lwjgl.NondirectBufferWrapper;
import org.lwjgl.opengl.ARBTextureFloat;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import de.sofd.viskit.model.LookupTable;

public class LWJGLLookupTableTextureManager extends LookupTableTextureManager {


    
    private static final LookupTableTextureManager lutManager = new LWJGLLookupTableTextureManager();
    
    private LWJGLLookupTableTextureManager() {
        
    }
    
    public static LookupTableTextureManager getInstance() {
        return lutManager;
    }
    
    @Override
    public TextureRef bindLutTexture(Object glContext, int texUnit, Map<String, Object> sharedContextData,
            LookupTable lut) {
        if (lut == null) {
            return null;
        }
        
        TextureRefStore texRefStore = (TextureRefStore) sharedContextData.get(TEX_STORE);
        if (null == texRefStore) {
            System.out.println("CREATING NEW TextureRefStore");
            texRefStore = new TextureRefStore();
            sharedContextData.put(TEX_STORE, texRefStore);
        }
        TextureRef texRef = texRefStore.getTexRef(lut);
        
        if (null == texRef) {
            logger.debug("need to create LUT texture for: " + lut);
            int texId = GL11.glGenTextures();
            GL11.glEnable(GL11.GL_TEXTURE_1D);
            GL13.glActiveTexture(texUnit);
            GL11.glBindTexture(GL11.GL_TEXTURE_1D, texId);
            FloatBuffer lutToUse = lut.getRGBAValues();
            GL11.glTexImage1D(
                    GL11.GL_TEXTURE_1D,   // target
                    0,                  // level
                    ARBTextureFloat.GL_RGBA32F_ARB,      // internalFormat
                    lutToUse.capacity() / 4,  //width
                    0,                  // border
                    GL11.GL_RGBA,         // format
                    GL11.GL_FLOAT,        // type
                    NondirectBufferWrapper.wrapDirect(lutToUse)// data
                    );
            GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            texRef = new TextureRef(texId);
            texRefStore.putTexRef(lut, texRef);
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
