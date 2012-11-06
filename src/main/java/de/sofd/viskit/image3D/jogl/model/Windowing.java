package de.sofd.viskit.image3D.jogl.model;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import java.nio.*;

import javax.media.opengl.*;

import com.sun.opengl.util.*;

import de.sofd.util.*;
import de.sofd.viskit.image3D.model.*;

public class Windowing {
    protected ShortBuffer buffer;

    protected ShortBuffer orgBuffer;

    /**
     * OpenGL-Id fuer Windowingfunktion.
     */
    protected int texId = -1;

    public Windowing(ShortBuffer buffer) {
        this.orgBuffer = buffer;

        this.buffer = BufferUtil.copyShortBuffer(buffer);
        this.buffer.rewind();
    }

    public void bindTexture(GL2 gl) {
        // gl.glPixelStorei( GL_UNPACK_ALIGNMENT, 1 );

        gl.glBindTexture(GL_TEXTURE_2D, texId);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA16F, 2, buffer.capacity() / 2, 0, GL_ALPHA, GL_SHORT, buffer);
    }

    public void cleanUp(GL2 gl) {
        if (texId != -1)
            deleteTex(texId, gl);

        texId = -1;

    }

    public void createTexture(GL2 gl) {
        gl.glEnable(GL_TEXTURE_2D);

        int[] texIds = new int[1];
        gl.glGenTextures(1, texIds, 0);

        texId = texIds[0];

        // for (int i = 0; i < windowing.capacity(); ++i)
        // System.out.println("win : " + windowing.get(i));

        // gl.glPixelStorei( GL_UNPACK_ALIGNMENT, 1 );

        gl.glBindTexture(GL_TEXTURE_2D, texId);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA16F, 2, buffer.capacity() / 2, 0, GL_ALPHA, GL_SHORT, buffer);

        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST
        // );
        // gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST
        // );

        gl.glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

        gl.glDisable(GL_TEXTURE_2D);

    }

    private void deleteTex(int texId, GL2 gl) {
        int[] tex = new int[] { texId };

        gl.glDeleteTextures(1, tex, 0);

    }

    public ShortBuffer getBuffer() {
        return buffer;
    }

    public void reloadOriginal() {
        for (int i = 0; i < buffer.capacity(); ++i)
            buffer.put(i, orgBuffer.get(i));

    }

    public void setBuffer(ShortBuffer buffer) {
        this.buffer = buffer;
    }

    public void updateCenter(short value, WindowingMode windowingMode, int z, ShortRange range) {
        switch (windowingMode) {
            case WINDOWING_MODE_LOCAL:
                setCenter(z, value);
    
                break;
            case WINDOWING_MODE_GLOBAL_RELATIVE:
                short delta = (short) (value - getCenter(z));
    
                for (int i = 0; i < buffer.capacity() / 2; ++i) {
                    short orgValue = getCenter(i);
                    setCenter(i, (short) Math.max(Math.min(range.getMax(), orgValue + delta), range.getMin()));
                }
    
                break;
            case WINDOWING_MODE_GLOBAL_ABSOLUTE:
                for (int i = 0; i < buffer.capacity() / 2; ++i) {
                    setCenter(i, value);
                }
    
                break;
            }
    }

    public void updateWidth(short value, WindowingMode windowingMode, int z, ShortRange range) {
        switch (windowingMode) {
            case WINDOWING_MODE_LOCAL:
                setWidth(z, value);
    
                break;
            case WINDOWING_MODE_GLOBAL_RELATIVE:
                short delta = (short) (value - getWidth(z));
    
                for (int i = 0; i < buffer.capacity() / 2; ++i) {
                    short orgValue = getWidth(i);
                    setWidth(i, (short) Math.max(Math.min(range.getMax(), orgValue + delta), range.getMin()));
                }
    
                break;
            case WINDOWING_MODE_GLOBAL_ABSOLUTE:
                for (int i = 0; i < buffer.capacity() / 2; ++i) {
                    setWidth(i, value);
                }
    
                break;
        }

    }

    public short getCenter(int z) {
        return buffer.get(z * 2);
    }
    
    public void setCenter(int z, short value) {
        buffer.put(z * 2, value);
    }

    public short getWidth(int z) {
        return buffer.get(z * 2 + 1);
    }
    
    public void setWidth(int z, short value) {
        buffer.put(z * 2 + 1, value);
    }

}