package de.sofd.viskit.util;

import java.awt.image.*;
import java.nio.*;

public class LutFunction {
    protected BufferedImage bimg;
    protected FloatBuffer buffer;

    public LutFunction(BufferedImage bimg, FloatBuffer buffer) {
        super();
        this.bimg = bimg;
        this.buffer = buffer;
    }

    public BufferedImage getBimg() {
        return bimg;
    }

    public FloatBuffer getBuffer() {
        return buffer;
    }

    public void setBimg(BufferedImage bimg) {
        this.bimg = bimg;
    }

    public void setBuffer(FloatBuffer buffer) {
        this.buffer = buffer;
    }

}