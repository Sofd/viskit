package de.sofd.viskit.controllers.cellpaint.texturemanager;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.sofd.viskit.model.ImageListViewModelElement;

/**
 * 
 * @author honglinh
 *
 */
public abstract class ImageTextureManager {
        
    protected static final Logger logger = Logger.getLogger(ImageTextureManager.class);
    
    protected final String TEX_STORE = "texturesStore"; 
    
    public static class TextureRef {
        private final int texId;
        private final int memorySize;
        private final float preScale;
        private final float preOffset;
        

        private int bottom;
        private int top;
        private int left;
        private int right;
        
        public TextureRef(int texId, int memorySize, float preScale, float preOffset, boolean flipVertically) {
            this.texId = texId;
            this.memorySize = memorySize;
            this.preScale = preScale;
            this.preOffset = preOffset;
            if (flipVertically) {
                left = 0;
                bottom = 1;
                right = 1;
                top = 0;
            } 
            left = 0;
            bottom = 0;
            right = 1;
            top = 1;            
        }

        public int getTexId() {
            return texId;
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
        
        public int bottom() {
            return bottom;
        }

        public int top() {
            return top;
        }

        public int left() {
            return left;
        }

        public int right() {
            return right;
        }
    }
    
    
    protected class TextureRefStore {
        private long totalMemConsumption = 0;
        private final long maxMemConsumption;

        private final LinkedHashMap<Object, TextureRef> texRefsByImageKey = new LinkedHashMap<Object, TextureRef>(256, 0.75F, true);

        public TextureRefStore(long maxMemConsumption) {
            this.maxMemConsumption = maxMemConsumption;
        }

        public boolean containsTextureFor(ImageListViewModelElement elt) {
            return texRefsByImageKey.containsKey(elt.getKey());
        }

        public TextureRef getTexRef(ImageListViewModelElement elt) {
            return texRefsByImageKey.get(elt.getKey());
        }

        public TextureRef getTexRef(Object imageKey) {
            return texRefsByImageKey.get(imageKey);
        }

        public void putTexRef(ImageListViewModelElement elt, TextureRef texRef) {
            texRefsByImageKey.put(elt.getKey(), texRef);
            totalMemConsumption += texRef.getMemorySize();
            freeExcessTextureMemory();
        }

        public void freeExcessTextureMemory() {
            while ((totalMemConsumption > maxMemConsumption) && (texRefsByImageKey.size() > 1)) {
                // ^^^ ensure size >= 1 to always keep at least the latest texture in, even if it alone exceeds maxMemConsumption
                //   (if that one wasn't loaded, we couldn't render it)
                Map.Entry<Object, TextureRef> oldestEntry = texRefsByImageKey.entrySet().iterator().next();
                logger.debug("deleting texture to free up memory: " + oldestEntry.getKey());
                TextureRef oldestTexRef = oldestEntry.getValue();
                glDeleteTextures(null, 1, new int[]{oldestTexRef.getTexId()}, 0);
                totalMemConsumption -= oldestTexRef.getMemorySize();
                texRefsByImageKey.remove(oldestEntry.getKey());
            }
        }

        public long getTotalMemConsumption() {
            return totalMemConsumption;
        }
    }
   
    
    protected TextureRefStore createRefStore(Map<String, Object> sharedContextData) {
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
        return texRefStore;
    }
    
    
    protected static int estimatedMemorySize(Buffer buffer) {
        int bufferSize = 0;
        if (buffer == null) {
            return bufferSize;
        }
        if (buffer instanceof ByteBuffer) {
            bufferSize = buffer.capacity() * 1;
        } else if (buffer instanceof IntBuffer) {
            bufferSize = buffer.capacity() * 4;
        } else if (buffer instanceof ShortBuffer) {
            bufferSize = buffer.capacity() * 2;
        } else if (buffer instanceof FloatBuffer) {
            bufferSize = buffer.capacity() * 4;
        } else if (buffer instanceof DoubleBuffer) {
            bufferSize = buffer.capacity() * 8;
        } else
            throw new RuntimeException("Unexpected buffer type " + buffer.getClass().getName());
        return bufferSize;
    }
    
    public abstract TextureRef bindImageTexture(Object glContext, int texUnit, Map<String, Object> sharedContextData, ImageListViewModelElement elt);
    
    public abstract void unbindCurrentImageTexture(Object glContext);
    
    protected abstract void glDeleteTextures(Object glContext, int n, int[] textures, int textures_offset);
}