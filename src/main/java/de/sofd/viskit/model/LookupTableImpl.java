package de.sofd.viskit.model;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;


public class LookupTableImpl implements LookupTable {

    protected final String name;
    protected final FloatBuffer rgbaValues;

    protected FloatBuffer rgba256floatValues;
    protected IntBuffer rgba256intValues;
    protected float[][] rgbaFloatArrays;
    protected float[][] rgba256floatArrays;
    protected int[][] rgba256intArrays;
    
    public LookupTableImpl(String name, FloatBuffer rgbaValues) {
        this.name = name;
        this.rgbaValues = rgbaValues;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FloatBuffer getRGBAValues() {
        return rgbaValues;
    }

    @Override
    public FloatBuffer getRGBA256floatValues() {
        if (null == rgba256floatValues) {
            int n = rgbaValues.limit();
            rgba256floatValues = FloatBuffer.allocate(n);
            for (int i = 0; i < n; i++) {
                rgba256floatValues.put(i, 255 * rgbaValues.get(i));
            }
        }
        return rgba256floatValues;
    }

    @Override
    public IntBuffer getRGBA256intValues() {
        if (null == rgba256intValues) {
            int n = rgbaValues.limit();
            rgba256intValues = IntBuffer.allocate(n);
            for (int i = 0; i < n; i++) {
                rgba256intValues.put(i, (int)(255 * rgbaValues.get(i)));
            }
        }
        return rgba256intValues;
    }

    @Override
    public float[][] getRGBAfloatArrays() {
        if (null == rgbaFloatArrays) {
            int n = rgbaValues.limit() / 4;
            rgbaFloatArrays = new float[n][];
            for (int i = 0; i < n; i++) {
                rgbaValues.position(4*i);
                rgbaFloatArrays[i] = new float[4];
                rgbaValues.get(rgbaFloatArrays[i]);
            }
        }
        return rgbaFloatArrays;
    }

    @Override
    public float[][] getRGBA256floatArrays() {
        if (null == rgba256floatArrays) {
            int n = rgbaValues.limit() / 4;
            rgba256floatArrays = new float[n][];
            for (int i = 0; i < n; i++) {
                rgbaValues.position(4*i);
                rgba256floatArrays[i] = new float[4];
                rgba256floatArrays[i][0] = 255 * rgbaValues.get();
                rgba256floatArrays[i][1] = 255 * rgbaValues.get();
                rgba256floatArrays[i][2] = 255 * rgbaValues.get();
                rgba256floatArrays[i][3] = 255 * rgbaValues.get();
            }
        }
        return rgba256floatArrays;
    }

    @Override
    public int[][] getRGBA256intArrays() {
        if (null == rgba256intArrays) {
            int n = rgbaValues.limit() / 4;
            rgba256intArrays = new int[n][];
            for (int i = 0; i < n; i++) {
                rgbaValues.position(4*i);
                rgba256intArrays[i] = new int[4];
                rgba256intArrays[i][0] = (int)(255 * rgbaValues.get());
                rgba256intArrays[i][1] = (int)(255 * rgbaValues.get());
                rgba256intArrays[i][2] = (int)(255 * rgbaValues.get());
                rgba256intArrays[i][3] = (int)(255 * rgbaValues.get());
            }
        }
        return rgba256intArrays;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LookupTableImpl other = (LookupTableImpl) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return name;
    }

    
}
