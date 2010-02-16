package de.sofd.viskit.ui.imagelist.cellviewers.jogl;

import static javax.media.opengl.GL.GL_LINEAR;
import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_S;
import static javax.media.opengl.GL2.GL_CLAMP;
import static javax.media.opengl.GL2GL3.GL_TEXTURE_1D;

import java.nio.FloatBuffer;
import java.util.LinkedHashMap;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.apache.log4j.Logger;

import de.sofd.lang.Runnable2;
import de.sofd.viskit.model.LookupTable;

/**
 *
 * @author olaf
 */
public class LookupTableTextureManager {

    protected static final Logger logger = Logger.getLogger(LookupTableTextureManager.class);

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
        private final LinkedHashMap<LookupTable, TextureRef> texRefsByImageKey = new LinkedHashMap<LookupTable, TextureRef>(256, 0.75F, true);

        public boolean containsTextureFor(LookupTable lut) {
            return texRefsByImageKey.containsKey(lut);
        }

        public TextureRef getTexRef(LookupTable lut) {
            return texRefsByImageKey.get(lut);
        }

        public void putTexRef(LookupTable lut, TextureRef texRef, GL gl) {
            texRefsByImageKey.put(lut, texRef);
        }

    }

    // TODO: ugly staticnesses in here...

    private static boolean initialized = false;
    private static final String TEX_STORE = "lutTexturesStore";

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

    private static FloatBuffer testLut;
    private static int nComponents = 4;
    
    static {
        init();
        int n = 25;
        testLut = FloatBuffer.allocate(n*nComponents);
        for (int i = 0; i < n; i++) {
            testLut.put(i*nComponents, (float)i/n);
            //testLut.put(i*4, 0.7f);
            testLut.put(i*nComponents+1, 0.0f);
            testLut.put(i*nComponents+2, 1.0f - (float)i/n);
            if (nComponents >= 4) {
                testLut.put(i*nComponents+3, 1.0f);
            }
        }
    }

    public static TextureRef bindLutTexture(SharedContextData cd, LookupTable lut) {
        if (lut == null) {
            return null;
        }
        TextureRefStore texRefStore = (TextureRefStore) cd.getAttribute(TEX_STORE);
        TextureRef texRef = texRefStore.getTexRef(lut);
        
        if (null == texRef) {
            logger.info("need to create LUT texture for: " + lut);
            GL2 gl = cd.getGlContext().getCurrentGL().getGL2();
            int[] texId = new int[1];
            gl.glGenTextures(1, texId, 0);
            cd.getGlContext().getCurrentGL().glEnable(GL2.GL_TEXTURE_1D);
            cd.getGlContext().getCurrentGL().glActiveTexture(GL2.GL_TEXTURE2);
            gl.glBindTexture(gl.GL_TEXTURE_1D, texId[0]);
            FloatBuffer lutToUse = lut.getRGBAValues();
            //FloatBuffer lutToUse = testLut;
            gl.glTexImage1D(
                    gl.GL_TEXTURE_1D,   // target
                    0,                  // level
                    gl.GL_RGBA32F,      // internalFormat
                    lutToUse.capacity() / nComponents,  //width
                    0,                  // border
                    gl.GL_RGBA,         // format
                    gl.GL_FLOAT,        // type
                    lutToUse            // data
                    );
            gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_WRAP_S, GL_CLAMP);
            gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            texRef = new TextureRef(texId[0]);
            texRefStore.putTexRef(lut, texRef, cd.getGlContext().getCurrentGL());
        }
        cd.getGlContext().getCurrentGL().glEnable(GL2.GL_TEXTURE_1D);
        cd.getGlContext().getCurrentGL().glActiveTexture(GL2.GL_TEXTURE2);
        cd.getGlContext().getCurrentGL().glBindTexture(GL2.GL_TEXTURE_1D, texRef.getTexId());
        return texRef;
    }

    public static void unbindCurrentLutTexture(SharedContextData cd) {
        cd.getGlContext().getCurrentGL().glBindTexture(GL.GL_TEXTURE_2D, 0);
    }
}
