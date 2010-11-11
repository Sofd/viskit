package de.sofd.viskit.controllers.cellpaint.texturemanager;

import static javax.media.opengl.GL2GL3.GL_TEXTURE_1D;

import java.nio.FloatBuffer;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import de.sofd.viskit.model.LookupTable;

public class JGLLookupTableTextureManager extends LookupTableTextureManager {
    
    private static final LookupTableTextureManager lutManager = new JGLLookupTableTextureManager();
    
    private JGLLookupTableTextureManager() {
        
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
        GL2 gl = getGL2(glContext);
        
        TextureRefStore texRefStore = (TextureRefStore) sharedContextData.get(TEX_STORE);
        if (null == texRefStore) {
            System.out.println("CREATING NEW TextureRefStore");
            texRefStore = new TextureRefStore();
            sharedContextData.put(TEX_STORE, texRefStore);
        }
        TextureRef texRef = texRefStore.getTexRef(lut);
        
        if (null == texRef) {
            logger.debug("need to create LUT texture for: " + lut);
            int[] texId = new int[1];
            gl.glGenTextures(1, texId, 0);
            gl.glEnable(GL2.GL_TEXTURE_1D);
            gl.glActiveTexture(texUnit);
            gl.glBindTexture(gl.GL_TEXTURE_1D, texId[0]);
            FloatBuffer lutToUse = lut.getRGBAValues();
            gl.glTexImage1D(
                    gl.GL_TEXTURE_1D,   // target
                    0,                  // level
                    gl.GL_RGBA32F,      // internalFormat
                    lutToUse.capacity() / 4,  //width
                    0,                  // border
                    gl.GL_RGBA,         // format
                    gl.GL_FLOAT,        // type
                    lutToUse            // data
                    );
            gl.glTexParameteri(GL_TEXTURE_1D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
            gl.glTexParameteri(GL_TEXTURE_1D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
            gl.glTexParameteri(GL_TEXTURE_1D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
            texRef = new TextureRef(texId[0]);
            texRefStore.putTexRef(lut, texRef);
        }
        gl.glEnable(GL2.GL_TEXTURE_1D);
        gl.glActiveTexture(texUnit);
        gl.glBindTexture(GL2.GL_TEXTURE_1D, texRef.getTexId());
        return texRef;
    }

    @Override
    public void unbindCurrentLutTexture(Object glContext) {
        GL2 gl = getGL2(glContext);
        gl.glBindTexture(GL2.GL_TEXTURE_1D, 0);
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