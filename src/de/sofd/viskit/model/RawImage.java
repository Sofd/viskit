package de.sofd.viskit.model;

import java.awt.image.BufferedImage;
import java.nio.Buffer;

/**
 * A pixel image. Functionally equivalent to {@link BufferedImage}, potentially
 * more efficient because it uses a NIO buffer for holding the pixel data, which
 * may be faster to read from backing store, and faster to process for users.
 *
 * @author olaf
 */
public interface RawImage {

    public int getWidth();

    public int getHeight();

    /**
     *
     * @return one of the PIXEL_FORMAT_* constants, describing the
     *         pixel format of the data in #getPixelData()
     */
    public int getPixelFormat();

    /**
     *
     * @return one of the PIXEL_TYPE_* constants, describing the
     *         pixel type of the data in #getPixelData()
     */
    public int getPixelType();

    /**
     *
     * @return the pixel data, in row-major form, with one element in
     *         the buffer holding one element (channel) of a pixel. Will thus
     *         contain getWidth()*getHeight()*(number of channels, defined by getPixelFormat())
     *         elements.
     */
    public Buffer getPixelData();

    /**
     * Unsupported pixel format
     */
    public static final int PIXEL_FORMAT_NOT_SUPPORTED = -1;

    /**
     * Each pixel in getPixelData() consists of one
     * element of type getPixelType(), containing the luminance value
     */
    public static final int PIXEL_FORMAT_LUMINANCE = 1;

    /**
     * Each pixel in getPixelData() consists of three
     * elements of type getPixelType(), containing the R, G and B values
     */
    public static final int PIXEL_FORMAT_RGB = 2;

    /**
     * Unsupported pixel type
     */
    public static final int PIXEL_TYPE_NOT_SUPPORTED = -1;

    /**
     * stored in bytes. getPixelData() instanceof ByteBuffer
     */
    public static final int PIXEL_TYPE_UNSIGNED_BYTE = 1;

    /**
     * stored in shorts. getPixelData() instanceof ShortBuffer
     */
    public static final int PIXEL_TYPE_SIGNED_12BIT = 2;

    /**
     * stored in shorts. getPixelData() instanceof ShortBuffer
     */
    public static final int PIXEL_TYPE_UNSIGNED_12BIT = 3;

    /**
     * stored in shorts. getPixelData() instanceof ShortBuffer
     */
    public static final int PIXEL_TYPE_SIGNED_16BIT = 4;

    /**
     * stored in ints. getPixelData() instanceof IntBuffer
     */
    public static final int PIXEL_TYPE_UNSIGNED_16BIT = 5;
}
