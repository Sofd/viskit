package de.sofd.viskit.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class NioBufferUtil {

    public static ShortBuffer newShortBuffer(int n)
    {
        return newByteBuffer(n * 2).asShortBuffer();
    }

    public static FloatBuffer newFloatBuffer(int n) {
        return newByteBuffer(n * 4).asFloatBuffer();
    }

    public static ByteBuffer newByteBuffer(int numElements) {
        ByteBuffer bb = ByteBuffer.allocateDirect(numElements);
        bb.order(ByteOrder.nativeOrder());
        return bb;
    }

}
